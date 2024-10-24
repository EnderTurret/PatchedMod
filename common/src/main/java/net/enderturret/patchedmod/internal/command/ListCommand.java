package net.enderturret.patchedmod.internal.command;

import static net.enderturret.patchedmod.internal.command.PatchedCommand.translate;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;

import net.enderturret.patchedmod.Patched;
import net.enderturret.patchedmod.internal.env.IEnvironment;
import net.enderturret.patchedmod.util.IPatchingPackResources;
import net.enderturret.patchedmod.util.PatchUtil;
import net.enderturret.patchedmod.util.meta.IPattern;
import net.enderturret.patchedmod.util.meta.PatchTarget;

/**
 * Defines the '/patched list' subcommand, which handles providing lists of the packs with patches and the patches in those packs.
 * @author EnderTurret
 */
final class ListCommand {

	static <T> LiteralArgumentBuilder<T> create(IEnvironment<T> env) {
		return env.literal("list")
				.then(env.literal("patches")
						.then(env.argument("pack", StringArgumentType.greedyString())
								.suggests((ctx, builder) -> PatchedCommand.suggestPack(ctx, builder, env, false))
								.executes(ctx -> listPatches(ctx, env))))
				.then(env.literal("packs").executes(ctx -> listPacks(ctx, env, false))
						.then(env.literal("verbose").executes(ctx -> listPacks(ctx, env, true))));
	}

	@SuppressWarnings("resource")
	private static <T> int listPatches(CommandContext<T> ctx, IEnvironment<T> env) {
		final String packName = StringArgumentType.getString(ctx, "pack");
		final ResourceManager man = env.getResourceManager(ctx.getSource());

		final List<PackResources> packs = Patched.platform().getExpandedPacks(man)
				.filter(p -> packName.equals(Patched.platform().getName(p)))
				.toList();

		if (packs.isEmpty()) {
			env.sendFailure(ctx.getSource(), translate("command.patched.list.pack_not_found", "That pack doesn't exist."));
			return 0;
		}

		if (packs.size() > 1) {
			env.sendFailure(ctx.getSource(), translate("command.patched.list.too_many_packs", "There is more than one pack with that name."));
			return 0;
		}

		final PackResources pack = packs.get(0);

		if (!Patched.platform().hasPatches(pack)) {
			env.sendFailure(ctx.getSource(), translate("command.patched.list.patching_disabled", "That pack doesn't have patches enabled."));
			return 0;
		}

		record Patch(String loc, @Nullable String ns, @Nullable String paths) {}

		final List<Patch> patches = new ArrayList<>();

		for (PackType type : PackType.values())
			for (String namespace : pack.getNamespaces(type))
				for (ResourceLocation loc : PatchUtil.getResources(pack, type, namespace, s -> s.getPath().endsWith(".patch")))
					patches.add(new Patch(loc.toString(), null, null));

		if (pack instanceof IPatchingPackResources ppp)
			for (PatchTarget patchTarget : ppp.patchedMetadata().patchTargets())
				for (PatchTarget.Target target : patchTarget.targets()) {
					final String ns = target.namespace().stream().map(IPattern::toString).collect(Collectors.joining("\", \"", "\"", "\""));
					final String paths = target.path().stream().map(IPattern::toString).collect(Collectors.joining("\", \"", "\"", "\""));
					patches.add(new Patch(patchTarget.patch(), ns, paths));
				}

		final boolean single = patches.size() == 1;

		final MutableComponent c = translate("command.patched.list.patches." + (single ? "single" : "multi"),
				single ? "There is 1 patch in %2$s:" : "There are %1$s patches in %2$s:",
				patches.size(), Patched.platform().getName(pack));

		final String command = ctx.getNodes().get(0).getNode().getName();

		for (Patch patch : patches) {
			final String safePackName = StringArgumentType.escapeIfRequired(Patched.platform().getName(pack));
			final boolean dynamic = patch.ns != null;

			c.append("\n").append(Component.literal(patch.loc)
					.setStyle(PatchedCommand.suggestCommand("/" + command + " dump patch " + safePackName + (dynamic ? " dynamic" : "") + " " + patch.loc)));

			if (dynamic)
				c.append(translate("command.patched.list.patches.dynamic",
						" (applying to namespaces %1$s and paths %2$s)",
						patch.ns, patch.paths));
		}

		env.sendSuccess(ctx.getSource(), c, false);

		return Command.SINGLE_SUCCESS;
	}

	private static <T> int listPacks(CommandContext<T> ctx, IEnvironment<T> env, boolean listAll) {
		final ResourceManager man = env.getResourceManager(ctx.getSource());

		record Entry(PackResources pack, String name, boolean patching) {}

		final List<Entry> packs = Patched.platform().getExpandedPacks(man)
				.map(p -> new Entry(p, Patched.platform().getName(p), Patched.platform().hasPatches(p)))
				.sorted(Comparator.comparing(Entry::name))
				.toList();

		final List<Entry> patching = packs.stream()
				.filter(Entry::patching)
				.toList();

		final boolean single = patching.size() == 1;

		final MutableComponent c = translate("command.patched.list.packs." + (single ? "single" : "multi"),
				single ? "There is 1 pack with patching enabled:" : "There are %1$s packs with patching enabled:",
				patching.size());

		final String command = ctx.getNodes().get(0).getNode().getName();

		for (Entry pack : patching) {
			final HoverEvent hover = listAll ? new HoverEvent(HoverEvent.Action.SHOW_TEXT,
					Component.literal(pack.pack.packId() + " (" + pack.pack.getClass().getSimpleName() + ")")) : null;

			c.append("\n  ").append(Component.literal(pack.name)
					.setStyle(PatchedCommand.suggestCommand("/" + command + " list patches " + pack.name)
							.withHoverEvent(hover)));
		}

		final List<Entry> notPatching = packs.stream()
				.filter(e -> !e.patching)
				.toList();

		if (listAll && !notPatching.isEmpty()) {
			c.append("\n\n").append(translate("command.patched.list.packs.verbose", "Additionally, the following packs do not have patching enabled:"));
			for (Entry pack : notPatching)
				c.append("\n  ").append(Component.literal(pack.name)
						.setStyle(Style.EMPTY.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
								Component.literal(pack.pack.packId() + " (" + pack.pack.getClass().getSimpleName() + ")")))));
		}

		env.sendSuccess(ctx.getSource(), c, false);

		return Command.SINGLE_SUCCESS;
	}
}
package net.enderturret.patchedmod.command;

import static net.enderturret.patchedmod.command.PatchedCommand.translate;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;

import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;

import net.enderturret.patchedmod.Patched;
import net.enderturret.patchedmod.util.PatchUtil;
import net.enderturret.patchedmod.util.env.IEnvironment;

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

		final PackResources pack = Patched.platform().getExpandedPacks(man)
				.filter(p -> packName.equals(Patched.platform().getName(p)))
				.findFirst()
				.orElse(null);

		if (pack == null) {
			env.sendFailure(ctx.getSource(), translate("command.patched.list.pack_not_found", "That pack doesn't exist."));
			return 0;
		}

		if (!Patched.platform().hasPatches(pack)) {
			env.sendFailure(ctx.getSource(), translate("command.patched.list.patching_disabled", "That pack doesn't have patches enabled."));
			return 0;
		}

		final List<ResourceLocation> patches = new ArrayList<>();

		for (PackType type : PackType.values())
			for (String namespace : pack.getNamespaces(type))
				patches.addAll(PatchUtil.getResources(pack, type, namespace, s -> s.getPath().endsWith(".patch")));

		final boolean single = patches.size() == 1;

		final MutableComponent c = translate("command.patched.list.patches." + (single ? "single" : "multi"),
				"There " + (!single ? "are" : "is")
				+ " " + patches.size() + " patch" + (!single ? "es" : "")
				+ " in " + Patched.platform().getName(pack) + ":", patches.size(), Patched.platform().getName(pack));

		final String command = ctx.getNodes().get(0).getNode().getName();

		for (ResourceLocation loc : patches) {
			final String patch = loc.toString();
			c.append("\n  ").append(Component.literal(patch)
					.setStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,
							"/" + command + " dump patch " + StringArgumentType.escapeIfRequired(Patched.platform().getName(pack)) + " " + patch))
							.withUnderlined(true)));
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
				"There " + (!single ? "are" : "is")
				+ " " + patching.size() + " pack" + (!single ? "s" : "")
				+ " with patching enabled:", patching.size());

		final String command = ctx.getNodes().get(0).getNode().getName();

		for (Entry pack : patching) {
			final ClickEvent click = new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,
					"/" + command + " list patches " + pack.name);
			final HoverEvent hover = listAll ? new HoverEvent(HoverEvent.Action.SHOW_TEXT,
					Component.literal(pack.pack.packId())) : null;

			c.append("\n  ").append(Component.literal(pack.name)
					.setStyle(Style.EMPTY.withClickEvent(click)
							.withHoverEvent(hover)
							.withUnderlined(true)));
		}

		final List<Entry> notPatching = packs.stream()
				.filter(e -> !e.patching)
				.toList();

		if (listAll && !notPatching.isEmpty()) {
			c.append("\n\n").append(translate("command.patched.list.packs.verbose", "Additionally, the following packs do not have patching enabled:"));
			for (Entry pack : notPatching)
				c.append("\n  ").append(Component.literal(pack.name)
						.setStyle(Style.EMPTY.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
								Component.literal(pack.pack.packId())))));
		}

		env.sendSuccess(ctx.getSource(), c, false);

		return Command.SINGLE_SUCCESS;
	}
}
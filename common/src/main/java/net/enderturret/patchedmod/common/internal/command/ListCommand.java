package net.enderturret.patchedmod.common.internal.command;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;

import net.enderturret.patchedmod.common.internal.PatchedInternal;
import net.enderturret.patchedmod.common.internal.env.IEnvironment;
import net.enderturret.patchedmod.common.internal.env.binding.PatchedMutableComponent;
import net.enderturret.patchedmod.common.internal.env.binding.PatchedPackResources;
import net.enderturret.patchedmod.common.internal.env.binding.PatchedResourceLocation;
import net.enderturret.patchedmod.common.internal.env.binding.PatchedResourceManager;
import net.enderturret.patchedmod.common.util.meta.IPattern;
import net.enderturret.patchedmod.common.util.meta.PatchTarget;
import net.enderturret.patchedmod.common.util.meta.PatchedPackType;

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
		final PatchedResourceManager man = env.getResourceManager(ctx.getSource());

		final List<PatchedPackResources> packs = man.patched$listPacks()
				.filter(p -> packName.equals(p.patched$getName()))
				.toList();

		if (packs.isEmpty()) {
			env.sendFailure(ctx.getSource(), "command.patched.list.pack_not_found", "That pack doesn't exist.");
			return 0;
		}

		if (packs.size() > 1) {
			env.sendFailure(ctx.getSource(), "command.patched.list.too_many_packs", "There is more than one pack with that name.");
			return 0;
		}

		final PatchedPackResources pack = packs.get(0);

		if (!pack.patched$hasPatches()) {
			env.sendFailure(ctx.getSource(), "command.patched.list.patching_disabled", "That pack doesn't have patches enabled.");
			return 0;
		}

		record Patch(String loc, @Nullable String ns, @Nullable String paths) {}

		final List<Patch> patches = new ArrayList<>();

		for (PatchedPackType type : PatchedPackType.values())
			for (String namespace : pack.patched$getNamespaces(type))
				for (PatchedResourceLocation loc : PatchedInternal.getResources(pack, type, namespace, s -> s.patched$getPath().endsWith(".patch")))
					patches.add(new Patch(loc.toString(), null, null));

		patches.sort(Comparator.comparing(Patch::loc));

		for (PatchTarget patchTarget : pack.patchedMetadata().patchTargets())
			for (PatchTarget.Target target : patchTarget.targets()) {
				final String ns = target.namespace().stream().map(IPattern::toString).collect(Collectors.joining("\", \"", "\"", "\""));
				final String paths = target.path().stream().map(IPattern::toString).collect(Collectors.joining("\", \"", "\"", "\""));
				patches.add(new Patch(patchTarget.patch(), ns, paths));
			}

		final boolean single = patches.size() == 1;

		final PatchedMutableComponent c = env.translate("command.patched.list.patches." + (single ? "single" : "multi"),
				single ? "There is 1 patch in %2$s:" : "There are %1$s patches in %2$s:",
				patches.size(), pack.patched$getName());

		final String command = ctx.getNodes().get(0).getNode().getName();

		for (Patch patch : patches) {
			final String safePackName = StringArgumentType.escapeIfRequired(pack.patched$getName());
			final boolean dynamic = patch.ns != null;

			c.append("\n").appendWithCommand(patch.loc, "/" + command + " dump patch " + safePackName + (dynamic ? " dynamic" : "") + " " + patch.loc);

			if (dynamic)
				c.append("command.patched.list.patches.dynamic",
						" (applying to namespaces %1$s and paths %2$s)",
						patch.ns, patch.paths);
		}

		env.sendSuccess(ctx.getSource(), false, c);

		return Command.SINGLE_SUCCESS;
	}

	private static <T> int listPacks(CommandContext<T> ctx, IEnvironment<T> env, boolean listAll) {
		final PatchedResourceManager man = env.getResourceManager(ctx.getSource());

		record Entry(PatchedPackResources pack, String name, boolean patching) {}

		final List<Entry> packs = man.patched$listPacks()
				.map(p -> new Entry(p, p.patched$getName(), p.patchedMetadata().patchingEnabled()))
				.sorted(Comparator.comparing(Entry::name))
				.toList();

		final List<Entry> patching = packs.stream()
				.filter(Entry::patching)
				.toList();

		final boolean single = patching.size() == 1;

		final PatchedMutableComponent c = env.translate("command.patched.list.packs." + (single ? "single" : "multi"),
				single ? "There is 1 pack with patching enabled:" : "There are %1$s packs with patching enabled:",
				patching.size());

		final String command = ctx.getNodes().get(0).getNode().getName();

		for (Entry pack : patching)
			c.append("\n  ").appendWithCommandHover(pack.name, "/" + command + " list patches " + pack.name,
					makeVerboseDescription(pack.pack));

		final List<Entry> notPatching = packs.stream()
				.filter(e -> !e.patching)
				.toList();

		if (listAll && !notPatching.isEmpty()) {
			c.append("\n\n").append("command.patched.list.packs.verbose", "Additionally, the following packs do not have patching enabled:");
			for (Entry pack : notPatching)
				c.append("\n  ").appendWithHover(pack.name, makeVerboseDescription(pack.pack));
		}

		env.sendSuccess(ctx.getSource(), false, c);

		return Command.SINGLE_SUCCESS;
	}

	private static String makeVerboseDescription(PatchedPackResources pack) {
		final Class<?> cls = pack.getClass();
		String clsName = cls.getSimpleName();
		if (clsName.isEmpty()) clsName = cls.getName().substring(cls.getPackageName().length() + 1);
		return pack.patched$packId() + " (" + clsName + ")";
	}
}
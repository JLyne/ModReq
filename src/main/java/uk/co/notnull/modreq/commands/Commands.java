/*
 * ModReq
 * Copyright (C) 2021 James Lyne
 *
 * Based on ModReq 1.2 (https://www.spigotmc.org/resources/modreq.57560/)
 * Copyright (C) 2019 Aladram and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package uk.co.notnull.modreq.commands;

import cloud.commandframework.Command;
import cloud.commandframework.CommandTree;
import cloud.commandframework.annotations.*;
import cloud.commandframework.annotations.specifier.Greedy;
import cloud.commandframework.arguments.parser.ParserParameters;
import cloud.commandframework.bukkit.CloudBukkitCapabilities;
import cloud.commandframework.execution.AsynchronousCommandExecutionCoordinator;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.extra.confirmation.CommandConfirmationManager;
import cloud.commandframework.meta.CommandMeta;
import cloud.commandframework.meta.SimpleCommandMeta;
import cloud.commandframework.minecraft.extras.MinecraftExceptionHandler;
import cloud.commandframework.minecraft.extras.MinecraftHelp;
import cloud.commandframework.paper.PaperCommandManager;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import uk.co.notnull.modreq.Messages;
import uk.co.notnull.modreq.ModReq;
import uk.co.notnull.modreq.Util;

public class Commands {
	private PaperCommandManager<CommandSender> paperCommandManager;
    private CommandConfirmationManager<CommandSender> confirmationManager;
    private AnnotationParser<CommandSender> annotationParser;
    private MinecraftHelp<CommandSender> minecraftHelp;

    private final CmdCheck cmdCheck;
    private final CmdClaim cmdClaim;
    private final CmdDone cmdDone;
    private final CmdElevate cmdElevate;
    private final CmdModreq cmdModreq;
    private final CmdComment cmdComment;
    private final CmdReopen cmdReopen;
    private final CmdTpid cmdTpid;

	public Commands(ModReq plugin) {
		cmdCheck = new CmdCheck(plugin);
		cmdClaim = new CmdClaim(plugin);
		cmdDone = new CmdDone(plugin);
		cmdElevate = new CmdElevate(plugin);
		cmdModreq = new CmdModreq(plugin);
		cmdComment = new CmdComment(plugin);
		cmdReopen = new CmdReopen(plugin);
		cmdTpid = new CmdTpid(plugin);

        final Function<CommandTree<CommandSender>, CommandExecutionCoordinator<CommandSender>> executionCoordinatorFunction =
                AsynchronousCommandExecutionCoordinator.<CommandSender>builder().build();

        try {
            paperCommandManager = new PaperCommandManager<>(
                    plugin,
                    executionCoordinatorFunction,
                    Function.identity(),
                    Function.identity());
        } catch (final Exception e) {
            plugin.getLogger().severe("Failed to initialize the command manager");
            /* Disable the plugin */
            plugin.getServer().getPluginManager().disablePlugin(plugin);
            return;
        }

        //
        // Create the Minecraft help menu system
        //
        this.minecraftHelp = new MinecraftHelp<>(
                /* Help Prefix */ "/mr",
                /* Audience mapper */ (sender) -> sender,
                /* Manager */ this.paperCommandManager
        );
        //
        // Register Brigadier mappings
        //
//        if (paperCommandManager.hasCapability(CloudBukkitCapabilities.NATIVE_BRIGADIER)) {
//            paperCommandManager.registerBrigadier();
//        }
        //
        // Register asynchronous completions
        //
        if (paperCommandManager.hasCapability(CloudBukkitCapabilities.ASYNCHRONOUS_COMPLETION)) {
            this.paperCommandManager.registerAsynchronousCompletions();
        }
        //
        // Create the confirmation manager. This allows us to require certain commands to be
        // confirmed before they can be executed
		//
		this.confirmationManager = new CommandConfirmationManager<>(
				/* Timeout */ 30L,
				/* Timeout unit */ TimeUnit.SECONDS,
				/* Action when confirmation is required */ context -> {
					String argument = context.getCommand().getArguments().get(1).getName();

					if(argument.equals("open") && !Util.isMod(context.getCommandContext().getSender())) {
						Messages.send((Player) context.getCommandContext().getSender(), "confirmation.reopen");
					} else {
						Messages.send((Player) context.getCommandContext().getSender(), "confirmation.generic");
					}


		},
				/* Action when no confirmation is pending */
							  sender -> Messages.send((Player) sender, "confirmation.nothing")
		);
        //
        // Register the confirmation processor. This will enable confirmations for commands that require it
        //
        this.confirmationManager.registerConfirmationProcessor(paperCommandManager);
        //
        // Create the annotation parser. This allows you to define commands using methods annotated with
        // @CommandMethod
        //
        final Function<ParserParameters, CommandMeta> commandMetaFunction = p ->
                 SimpleCommandMeta.builder().with(CommandMeta.DESCRIPTION, "No description").build();

        this.annotationParser = new AnnotationParser<>(
                /* Manager */ this.paperCommandManager,
                /* Command sender type */ CommandSender.class,
                /* Mapper for command meta instances */ commandMetaFunction
        );
        //
        // Override the default exception handlers
        //
        new MinecraftExceptionHandler<CommandSender>()
                .withInvalidSyntaxHandler()
                .withInvalidSenderHandler()
                .withNoPermissionHandler()
                .withArgumentParsingHandler()
                .withDecorator(
                        component -> Messages.get("general.PREFIX").append(Component.space()).append(component)
                ).apply(paperCommandManager, (sender) -> sender);

        this.constructCommands();
	}

	private void constructCommands() {
		final Command.Builder<CommandSender> builder = this.paperCommandManager.commandBuilder("mr");

		this.annotationParser.parse(this);

		this.paperCommandManager.command(builder.literal("confirm")
                .meta(CommandMeta.DESCRIPTION, "Confirm a pending command")
                .handler(this.confirmationManager.createConfirmationExecutionHandler()));
	}

	@CommandMethod("mr help [query]")
    @CommandDescription("Help menu")
    private void commandHelp(
            final @NonNull CommandSender sender,
            final @Argument("query") @Greedy String query
    ) {
        this.minecraftHelp.queryCommands(query == null ? "" : query, sender);
    }

	@CommandMethod("mr list [page]")
    @CommandDescription("List open modreqs which need attention")
	@CommandPermission("modreq.mod")
    private void commandList(
            final @NonNull Player player,
            final @Argument("page") Integer page
    ) {
		cmdCheck.checkOpenModreqs(player, page != null ? page : 1);
	}

	@CommandMethod("mr info <id> [updatesPage]")
    @CommandDescription("View information about a specific modreq")
    private void commandInfo(
            final @NonNull Player player,
            final @Argument("id") Integer id,
            final @Argument("updatesPage") Integer page
    ) {
		cmdCheck.checkSpecialModreq(player, id, page);
	}

	@CommandMethod("mr search <criteria>")
    @CommandDescription("Search for open modreqs containing specific text")
	@CommandPermission("modreq.mod")
    private void commandSearch(
            final @NonNull Player player,
            final @Argument("criteria") String criteria
    ) {
		cmdCheck.searchModreqs(player, criteria);
	}

	@CommandMethod("mr searchpage <page>")
    @CommandDescription("Search for open modreqs containing specific text")
	@CommandPermission("modreq.mod")
	@Hidden()
    private void commandSearch(
            final @NonNull Player player,
            final @Argument("page") Integer page
    ) {
		cmdCheck.searchModreqs(player, page);
	}

	@CommandMethod("mr claim <id>")
    @CommandDescription("Claim the specified modreq")
	@CommandPermission("modreq.mod")
    private void commandClaim(
            final @NonNull Player player,
            final @Argument("id") Integer id
    ) {
		cmdClaim.claimModReq(player, id, true);
	}

	@CommandMethod("mr unclaim <id>")
    @CommandDescription("Unclaim the specified modreq")
	@CommandPermission("modreq.mod")
    private void commandUnclaim(
            final @NonNull Player player,
            final @Argument("id") Integer id
    ) {
		cmdClaim.claimModReq(player, id, false);
	}

	@CommandMethod("mr close <id> <message>")
    @CommandDescription("Close the specified modreq")
    private void commandClose(
            final @NonNull Player player,
            final @Argument("id") Integer id,
            final @Argument("message") @Greedy String message
    ) {
		cmdDone.doneModReq(player, id, message);
	}

	@CommandMethod("mr open <id> <message>")
    @CommandDescription("Open the specified modreq")
	@Confirmation
    private void commandReopen(
            final @NonNull Player player,
            final @Argument("id") Integer id,
            final @Argument("message") @Greedy String message
    ) {
		cmdReopen.reopenModReq(player, id, message);
	}

	@CommandMethod("mr elevate <id>")
    @CommandDescription("Elevate the specified modreq")
	@CommandPermission("modreq.mod")
    private void commandElevate(
            final @NonNull Player player,
            final @Argument("id") Integer id
    ) {
		cmdElevate.elevateModReq(player, id);
	}

	@CommandMethod("mr tp <id>")
    @CommandDescription("Teleport to the location where the specified modreq was created")
	@CommandPermission("modreq.mod")
    private void commandTp(
            final @NonNull Player player,
            final @Argument("id") Integer id
    ) {
		cmdTpid.tpToModReq(player, id);
	}

	@CommandMethod("mr comment add <id> <message>")
    @CommandDescription("Add a comment to the specified modreq")
    private void commandCommentAdd(
            final @NonNull Player player,
            final @Argument("id") Integer id,
            final @Argument("message") @Greedy String message
    ) {
		cmdComment.addComment(player, id, true, message);
	}

	@CommandMethod("mr comment addprivate <id> <message>")
    @CommandDescription("Add a private comment, only visible to staff, to the specified modreq")
	@CommandPermission("modreq.mod")
    private void commandPrivateCommentAdd(
            final @NonNull Player player,
            final @Argument("id") Integer id,
            final @Argument("message") @Greedy String message
    ) {
		cmdComment.addComment(player, id, false, message);
	}

	@CommandMethod("mr comment remove <id> <commentid>")
    @CommandDescription("Remove the specified comment from the specified modreq")
	@CommandPermission("modreq.mod")
	@Confirmation
    private void commandCommentRemove(
            final @NonNull Player player,
            final @Argument("id") Integer id,
            final @Argument("commentid") Integer commentId
    ) {
		cmdComment.removeComment(player, id, commentId);
	}

	@ProxiedBy("modreq")
	@CommandMethod("mr create <message>")
    @CommandDescription("Create a modreq")
    private void commandCreate(
            final @NonNull Player player,
            final @Argument("message") @Greedy String message
    ) {
		cmdModreq.modreq(player, message);
	}

	@CommandMethod("modreq")
    @CommandDescription("View your created modreqs")
    private void commandMe(
            final @NonNull Player player
    ) {
		cmdModreq.checkPlayerModReqs(player, 1);
	}

	@CommandMethod("mr me [page]")
    @CommandDescription("View your created modreqs")
    private void commandMe(
            final @NonNull Player player,
            final @Argument("page") Integer page
    ) {
		cmdModreq.checkPlayerModReqs(player, page != null ? page : 1);
	}

	@CommandMethod("mr reload")
    @CommandDescription("Reload the configuration")
	@CommandPermission("modreq.admin")
    private void commandReload(final @NonNull CommandSender player) {
		ModReq.getPlugin().reloadConfiguration();

		if(ModReq.getPlugin().getConfiguration().isMySQL()) {
			player.sendMessage(ChatColor.WHITE + "Config and language file reloaded (database: MySQL).");
		} else {
			player.sendMessage(ChatColor.WHITE + "Config and language file reloaded (database: SQLite).");
		}
	}
}

package uk.co.notnull.modreq.commands;

import cloud.commandframework.Command;
import cloud.commandframework.CommandTree;
import cloud.commandframework.annotations.*;
import cloud.commandframework.annotations.specifier.Greedy;
import cloud.commandframework.arguments.parser.ParserParameters;
import cloud.commandframework.arguments.parser.StandardParameters;
import cloud.commandframework.bukkit.BukkitCommandMetaBuilder;
import cloud.commandframework.bukkit.CloudBukkitCapabilities;
import cloud.commandframework.execution.AsynchronousCommandExecutionCoordinator;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.extra.confirmation.CommandConfirmationManager;
import cloud.commandframework.meta.CommandMeta;
import cloud.commandframework.minecraft.extras.MinecraftExceptionHandler;
import cloud.commandframework.minecraft.extras.MinecraftHelp;
import cloud.commandframework.paper.PaperCommandManager;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import uk.co.notnull.modreq.ModReq;

public class Commands {
	private PaperCommandManager<CommandSender> paperCommandManager;
    private CommandConfirmationManager<CommandSender> confirmationManager;
    private AnnotationParser<CommandSender> annotationParser;
    private MinecraftHelp<CommandSender> minecraftHelp;

	public Commands(ModReq plugin) {
        final Function<CommandTree<CommandSender>, CommandExecutionCoordinator<CommandSender>> executionCoordinatorFunction =
                AsynchronousCommandExecutionCoordinator.<CommandSender>newBuilder().build();

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
                /* Audience mapper */ plugin.getBukkitAudiences()::sender,
                /* Manager */ this.paperCommandManager
        );
        //
        // Register Brigadier mappings
        //
        if (paperCommandManager.queryCapability(CloudBukkitCapabilities.BRIGADIER)) {
            paperCommandManager.registerBrigadier();
        }
        //
        // Register asynchronous completions
        //
        if (paperCommandManager.queryCapability(CloudBukkitCapabilities.ASYNCHRONOUS_COMPLETION)) {
            this.paperCommandManager.registerAsynchronousCompletions();
        }
        //
        // Create the confirmation manager. This allows us to require certain commands to be
        // confirmed before they can be executed
        //
        this.confirmationManager = new CommandConfirmationManager<>(
                /* Timeout */ 30L,
                /* Timeout unit */ TimeUnit.SECONDS,
                /* Action when confirmation is required */ context -> context.getCommandContext().getSender().sendMessage(
                ChatColor.RED + "Confirmation required. Confirm using /mr confirm."),
                /* Action when no confirmation is pending */ sender -> sender.sendMessage(
                ChatColor.RED + "You don't have any pending commands.")
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
                BukkitCommandMetaBuilder.builder()
                        // This will allow you to decorate commands with descriptions
                        .withDescription(p.get(StandardParameters.DESCRIPTION, "No description"))
                        .build();
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
                        component -> Component.text()
                                .append(Component.text("[", NamedTextColor.DARK_GRAY))
                                .append(Component.text("ModReq", NamedTextColor.GOLD))
                                .append(Component.text("] ", NamedTextColor.DARK_GRAY))
                                .append(component).build()
                ).apply(paperCommandManager, plugin.getBukkitAudiences()::sender);
        //
        // Create the commands
        //
        this.constructCommands();
	}

	private void constructCommands() {
		final Command.Builder<CommandSender> builder = this.paperCommandManager.commandBuilder("mr");

		this.annotationParser.parse(this);

		this.paperCommandManager.command(builder.literal("confirm")
                .meta("description", "Confirm a pending command")
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
		if(page != null) {
			(new CmdCheck()).checkOpenModreqs(player, page);
		} else {
			(new CmdCheck()).checkOpenModreqs(player);
		}
	}

	@CommandMethod("mr info <id>")
    @CommandDescription("View information about a specific modreq")
	@CommandPermission("modreq.mod")
    private void commandInfo(
            final @NonNull Player player,
            final @Argument("id") Integer id
    ) {
		(new CmdCheck()).checkSpecialModreq(player, id);
	}

	@CommandMethod("mr search <criteria>")
    @CommandDescription("Search for open modreqs containing specific text")
	@CommandPermission("modreq.mod")
    private void commandSearch(
            final @NonNull Player player,
            final @Argument("criteria") String criteria
    ) {
		(new CmdCheck()).searchOpenModreqs(player, criteria);
	}

	@CommandMethod("mr claim <id>")
    @CommandDescription("Claim the specified modreq")
	@CommandPermission("modreq.mod")
    private void commandClaim(
            final @NonNull Player player,
            final @Argument("id") Integer id
    ) {
		(new CmdClaim(ModReq.getPlugin())).claimModReq(player, id, true);
	}

	@CommandMethod("mr unclaim <id>")
    @CommandDescription("Unclaim the specified modreq")
	@CommandPermission("modreq.mod")
    private void commandUnclaim(
            final @NonNull Player player,
            final @Argument("id") Integer id
    ) {
		(new CmdClaim(ModReq.getPlugin())).claimModReq(player, id, false);
	}

	@CommandMethod("mr close <id> <message>")
    @CommandDescription("Close the specified modreq")
	@CommandPermission("modreq.mod")
    private void commandClose(
            final @NonNull Player player,
            final @Argument("id") Integer id,
            final @Argument("message") @Greedy String message
    ) {
		(new CmdDone(ModReq.getPlugin())).doneModReq(player, id, message);
	}

	@CommandMethod("mr open <id>")
    @CommandDescription("Open the specified modreq")
	@CommandPermission("modreq.mod")
    private void commandClose(
            final @NonNull Player player,
            final @Argument("id") Integer id
    ) {
		(new CmdReopen(ModReq.getPlugin())).reopenModReq(player, id);
	}

	@CommandMethod("mr elevate <id>")
    @CommandDescription("Elevate the specified modreq")
	@CommandPermission("modreq.mod")
    private void commandElevate(
            final @NonNull Player player,
            final @Argument("id") Integer id
    ) {
		(new CmdElevate(ModReq.getPlugin())).elevateModReq(player, id);
	}

	@CommandMethod("mr tp <id>")
    @CommandDescription("Teleport to the location where the specified modreq was created")
	@CommandPermission("modreq.mod")
    private void commandTp(
            final @NonNull Player player,
            final @Argument("id") Integer id
    ) {
		(new CmdTpid(ModReq.getPlugin())).tpToModReq(player, id);
	}

	@CommandMethod("mr note add <id> <message>")
    @CommandDescription("Add a note to the specified modreq")
	@CommandPermission("modreq.mod")
    private void commandNoteAdd(
            final @NonNull Player player,
            final @Argument("id") Integer id,
            final @Argument("message") @Greedy String message
    ) {
		(new CmdNote(ModReq.getPlugin())).addNote(player, id, message);
	}

	@CommandMethod("mr note remove <id> <noteid>")
    @CommandDescription("Remove the specified note from the specified modreq")
	@CommandPermission("modreq.mod")
	@Confirmation
    private void commandNoteRemove(
            final @NonNull Player player,
            final @Argument("id") Integer id,
            final @Argument("noteid") Integer noteId
    ) {
		(new CmdNote(ModReq.getPlugin())).removeNote(player, id, noteId);
	}

	@ProxiedBy("modreq")
	@CommandMethod("mr create <message>")
    @CommandDescription("Create a modreq")
    private void commandCreate(
            final @NonNull Player player,
            final @Argument("message") @Greedy String message
    ) {
		(new CmdModreq(ModReq.getPlugin())).modreq(player, message);
	}

	@ProxiedBy("modreq")
	@CommandMethod("mr me")
    @CommandDescription("View your created modreqs")
    private void commandMe(
            final @NonNull Player player
    ) {
		(new CmdModreq(ModReq.getPlugin())).checkPlayerModReqs(player);
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

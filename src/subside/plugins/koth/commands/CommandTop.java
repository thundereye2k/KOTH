package subside.plugins.koth.commands;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.Selection;

import subside.plugins.koth.areas.Area;
import subside.plugins.koth.areas.Koth;
import subside.plugins.koth.commands.CommandHandler.CommandCategory;
import subside.plugins.koth.datatable.DataTable;
import subside.plugins.koth.exceptions.AreaAlreadyExistException;
import subside.plugins.koth.exceptions.AreaNotExistException;
import subside.plugins.koth.exceptions.CommandMessageException;
import subside.plugins.koth.exceptions.KothNotExistException;
import subside.plugins.koth.modules.Lang;
import subside.plugins.koth.utils.IPerm;
import subside.plugins.koth.utils.MessageBuilder;
import subside.plugins.koth.utils.Perm;
import subside.plugins.koth.utils.Utils;

public class CommandTop extends AbstractCommand {

    public CommandTop(CommandCategory category) {
        super(category);
    }

    @Override
    public void run(final CommandSender sender, final String[] args) {
        if(!getPlugin().getConfigHandler().getDatabase().isEnabled())
            return;

        Bukkit.getScheduler().runTaskAsynchronously(getPlugin(), () -> {
            DataTable.SimpleQueryBuilder builder = getPlugin().getDataTable().getSQLBuilder();

            ResultSet result;
            try {
                int page = 1;

                if(args.length > 0){
                    try {
                        page = Integer.parseInt(args[0]);
                    } catch(NumberFormatException e){
                        new MessageBuilder(Lang.COMMAND_TOP_NOTANUMBER).buildAndSend(sender);
                        return;
                    }
                }

                result = builder.limit(10).offset((page - 1) * 10).execute();

                List<String> msgs = new ArrayList<>();
                msgs.addAll(new MessageBuilder(Lang.COMMAND_TOP_TITLE).buildArray());

                int i = (page - 1) * 10 + 1;
                while (result.next()) {
                    msgs.addAll(
                            new MessageBuilder(Lang.COMMAND_TOP_ENTRY)
                                    .id(i++)
                                    .times(""+result.getInt("result"))
                                    .capper(Bukkit.getOfflinePlayer(UUID.fromString(result.getString("player_uuid"))).getName())
                                    .buildArray()
                    );
                }

                msgs.addAll(new MessageBuilder(Lang.COMMAND_TOP_PAGE).times(""+page).buildArray());

                Utils.sendMsg(sender, msgs);
            } catch(SQLException e){
                e.printStackTrace();
            }
        });
    }

    @Override
    public IPerm getPermission() {
        return Perm.TOP;
    }

    @Override
    public String[] getCommands() {
        return new String[] {
                "top"
        };
    }

    @Override
    public String getUsage() {
        return "/koth top [page]";
    }

    @Override
    public String getDescription() {
        return "Shows the top list";
    }

}

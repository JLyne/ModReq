package uk.co.notnull.modreq.commands;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import uk.co.notnull.modreq.ModReq;
import uk.co.notnull.modreq.Note;
import uk.co.notnull.modreq.Request;

public class CmdCheck {
	public CmdCheck() {
	}

	public void checkOpenModreqs(final Player player) {
		checkOpenModreqs(player, 1);
	}

	public void checkOpenModreqs(final Player player, final int page) {
		BukkitRunnable runnable = new BukkitRunnable() {
			public void run() {
				try {
					Connection connection = ModReq.getPlugin().getSqlHandler().open();
					if (connection == null) {
						ModReq.getPlugin().sendMsg(player, "error.DATABASE-ERROR");
						return;
					}

					PreparedStatement pStatement;
					if (player.hasPermission("modreq.admin")) {
						pStatement = connection.prepareStatement("SELECT id,uuid,request,timestamp,claimed,elevated FROM modreq WHERE done='0'");
					} else {
						pStatement = connection.prepareStatement("SELECT id,uuid,request,timestamp,claimed,elevated FROM modreq WHERE done='0' AND elevated='0'");
					}

					ResultSet sqlres = pStatement.executeQuery();
					if (!sqlres.next()) {
						ModReq.getPlugin().sendMsg(player, "mod.check.NO-MODREQS");
					} else {
						ArrayList requests = new ArrayList();

						while(!sqlres.isAfterLast()) {
							requests.add(new Request(sqlres.getInt(1), sqlres.getString(2), sqlres.getString(3), sqlres.getLong(4), "", 0, 0, 0, sqlres.getString(5), "", "", 0L, 0, sqlres.getInt(6)));
							sqlres.next();
						}

						sqlres.close();
						pStatement.close();

						if (page < 1) {
							player.sendMessage(ModReq.getPlugin()
													   .getLanguageFile()
													   .getLangString("error.NUMBER-ERROR")
													   .replaceAll("%id", String.valueOf(page)));
							connection.close();
							return;
						}

						int resultPages;
						if (requests.size() % ModReq.getPlugin().getConfiguration().getModreqs_per_page() != 0) {
							resultPages = requests.size() / ModReq.getPlugin().getConfiguration().getModreqs_per_page() + 1;
						} else {
							resultPages = requests.size() / ModReq.getPlugin().getConfiguration().getModreqs_per_page();
						}

						if (page > resultPages) {
							player.sendMessage(ModReq.getPlugin().getLanguageFile().getLangString("error.PAGE-ERROR").replaceAll("%page", "" + page));
							connection.close();
							return;
						}

						int start = (page - 1) * ModReq.getPlugin().getConfiguration().getModreqs_per_page();
						int end = page * ModReq.getPlugin().getConfiguration().getModreqs_per_page();
						player.sendMessage(ModReq.getPlugin().getLanguageFile().getLangString("mod.check.1").replaceAll("%count", "" + requests.size()));

						for(int i = start; i < end && i < requests.size(); ++i) {
							OfflinePlayer requestUser = null;
							OfflinePlayer claimedUser = null;
							requestUser = ModReq.getPlugin().getOfflinePlayer(((Request)requests.get(i)).getUuid());
							if (!((Request)requests.get(i)).getClaimed().equals("")) {
								claimedUser = ModReq.getPlugin().getOfflinePlayer(((Request)requests.get(i)).getClaimed());
							}

							String status = "";
							if (((Request)requests.get(i)).getClaimed().equals("")) {
								status = status + ModReq.getPlugin().getLanguageFile().getLangString("general.OPEN");
							} else if (claimedUser.getName() != null) {
								status = status + "§a" + claimedUser.getName();
							} else {
								status = status + "§aunknown";
							}

							if (((Request)requests.get(i)).getElevated() != 0) {
								status = status + " " + ModReq.getPlugin().getLanguageFile().getLangString("general.ELEVATED");
							}

							pStatement = connection.prepareStatement("SELECT COUNT(id) FROM modreq_notes WHERE modreq_id=?");
							pStatement.setInt(1, ((Request)requests.get(i)).getId());
							sqlres = pStatement.executeQuery();
							if (sqlres != null && sqlres.next() && sqlres.getInt(1) > 0) {
								status = status + " " + ModReq.getPlugin().getLanguageFile().getLangString("general.NOTES");
							}

							sqlres.close();
							pStatement.close();
							String username = "";
							if (requestUser.getName() != null) {
								if (requestUser.isOnline()) {
									username = username + "§a" + requestUser.getName();
								} else {
									username = username + "§c" + requestUser.getName();
								}
							} else {
								username = username + "§cunknown";
							}

							String timestamp_formatted = ModReq.getPlugin().getFormat().format(((Request)requests.get(i)).getTimestamp());
							player.sendMessage(ModReq.getPlugin().getLanguageFile().getLangString("mod.check.2").replaceAll("%id", "" + ((Request)requests.get(i)).getId()).replaceAll("%status", status).replaceAll("%date", timestamp_formatted).replaceAll("%player", username));
							player.sendMessage(ModReq.getPlugin().getLanguageFile().getLangString("mod.check.3").replaceAll("%msg", ((Request)requests.get(i)).getRequest()));
						}

						player.sendMessage(ModReq.getPlugin().getLanguageFile().getLangString("mod.check.4").replaceAll("%page", "" + page).replaceAll("%allpages", "" + resultPages));
					}

					connection.close();
				} catch (SQLException var16) {
					var16.printStackTrace();
					ModReq.getPlugin().sendMsg(player, "error.DATABASE-ERROR");
				}

			}
		};
		runnable.runTaskAsynchronously(ModReq.getPlugin());
	}

	public void checkSpecialModreq(final Player player, final int id) {
		BukkitRunnable runnable = new BukkitRunnable() {
			public void run() {
				try {
					Connection connection = ModReq.getPlugin().getSqlHandler().open();
					if (connection == null) {
						ModReq.getPlugin().sendMsg(player, "error.DATABASE-ERROR");
						return;
					}

					PreparedStatement pStatement = connection.prepareStatement("SELECT uuid,request,timestamp,world,x,y,z,claimed,mod_uuid,mod_comment,mod_timestamp,done,elevated FROM modreq WHERE id=?");
					pStatement.setInt(1, id);
					ResultSet sqlres = pStatement.executeQuery();
					if (!sqlres.next()) {
						player.sendMessage(ModReq.getPlugin().getLanguageFile().getLangString("error.ID-ERROR").replaceAll("%id", "" + id));
					} else {
						Request request = new Request(id, sqlres.getString(1), sqlres.getString(2), sqlres.getLong(3), sqlres.getString(4), sqlres.getInt(5), sqlres.getInt(6), sqlres.getInt(7), sqlres.getString(8), sqlres.getString(9), sqlres.getString(10), sqlres.getLong(11), sqlres.getInt(12), sqlres.getInt(13));
						sqlres.close();
						pStatement.close();
						OfflinePlayer requestUser = null;
						OfflinePlayer claimedUser = null;
						OfflinePlayer modUser = null;
						requestUser = ModReq.getPlugin().getOfflinePlayer(request.getUuid());
						if (!request.getClaimed().equals("")) {
							claimedUser = ModReq.getPlugin().getOfflinePlayer(request.getClaimed());
						}

						if (!request.getMod_uuid().equals("")) {
							modUser = ModReq.getPlugin().getOfflinePlayer(request.getMod_uuid());
						}

						String status = "";
						if (request.getDone() == 0 && request.getClaimed().equals("")) {
							status = status + ModReq.getPlugin().getLanguageFile().getLangString("general.OPEN");
						} else if (request.getDone() == 0 && !request.getClaimed().equals("")) {
							if (claimedUser.getName() != null) {
								status = status + ModReq.getPlugin().getLanguageFile().getLangString("general.CLAIMED").replaceAll("%mod", claimedUser.getName());
							} else {
								status = status + ModReq.getPlugin().getLanguageFile().getLangString("general.CLAIMED").replaceAll("%mod", "unknown");
							}
						} else {
							status = status + ModReq.getPlugin().getLanguageFile().getLangString("general.CLOSED");
						}

						if (request.getElevated() != 0) {
							status = status + " " + ModReq.getPlugin().getLanguageFile().getLangString("general.ELEVATED");
						}

						String timestamp_formatted = ModReq.getPlugin().getFormat().format(request.getTimestamp());
						player.sendMessage(ModReq.getPlugin().getLanguageFile().getLangString("mod.check.special.1").replaceAll("%id", "" + id).replaceAll("%status", status));
						if (requestUser.getName() != null) {
							player.sendMessage(ModReq.getPlugin().getLanguageFile().getLangString("mod.check.special.2").replaceAll("%player", requestUser.getName()).replaceAll("%date", timestamp_formatted).replaceAll("%world", request.getWorld()).replaceAll("%x", "" + request.getX()).replaceAll("%y", "" + request.getY()).replaceAll("%z", "" + request.getZ()));
						} else {
							player.sendMessage(ModReq.getPlugin().getLanguageFile().getLangString("mod.check.special.2").replaceAll("%player", "unknown").replaceAll("%date", timestamp_formatted).replaceAll("%world", request.getWorld()).replaceAll("%x", "" + request.getX()).replaceAll("%y", "" + request.getY()).replaceAll("%z", "" + request.getZ()));
						}

						player.sendMessage(ModReq.getPlugin().getLanguageFile().getLangString("mod.check.special.3").replaceAll("%msg", request.getRequest()));
						if (!request.getMod_uuid().equals("")) {
							String mod_timestamp_formatted = ModReq.getPlugin().getFormat().format(request.getMod_timestamp());
							if (modUser.getName() != null) {
								player.sendMessage(ModReq.getPlugin().getLanguageFile().getLangString("mod.check.special.4").replaceAll("%mod", modUser.getName()).replaceAll("%date", mod_timestamp_formatted));
							} else {
								player.sendMessage(ModReq.getPlugin().getLanguageFile().getLangString("mod.check.special.4").replaceAll("%mod", "unknown").replaceAll("%date", mod_timestamp_formatted));
							}

							player.sendMessage(ModReq.getPlugin().getLanguageFile().getLangString("mod.check.special.5").replaceAll("%msg", request.getMod_comment()));
						} else {
							pStatement = connection.prepareStatement("SELECT id,uuid,note FROM modreq_notes WHERE modreq_id=? ORDER BY id ASC");
							pStatement.setInt(1, id);
							sqlres = pStatement.executeQuery();
							if (sqlres.next()) {
								ArrayList notes = new ArrayList();

								while(!sqlres.isAfterLast()) {
									notes.add(new Note(sqlres.getInt(1), id, sqlres.getString(2), sqlres.getString(3)));
									sqlres.next();
								}

								for(int i = 0; i < notes.size(); ++i) {
									OfflinePlayer mod = ModReq.getPlugin().getOfflinePlayer(((Note)notes.get(i)).getUuid());
									if (mod.getName() != null) {
										player.sendMessage(ModReq.getPlugin().getLanguageFile().getLangString("mod.check.special.6").replaceAll("%id", "" + i).replaceAll("%mod", mod.getName()).replaceAll("%msg", ((Note)notes.get(i)).getNote()));
									} else {
										player.sendMessage(ModReq.getPlugin().getLanguageFile().getLangString("mod.check.special.6").replaceAll("%id", "" + i).replaceAll("%mod", "unknown").replaceAll("%msg", ((Note)notes.get(i)).getNote()));
									}
								}
							}

							sqlres.close();
							pStatement.close();
						}
					}

					connection.close();
				} catch (SQLException var15) {
					var15.printStackTrace();
					ModReq.getPlugin().sendMsg(player, "error.DATABASE-ERROR");
				}

			}
		};
		runnable.runTaskAsynchronously(ModReq.getPlugin());
	}

	public void searchOpenModreqs(final Player player, final String criteria) {
		BukkitRunnable runnable = new BukkitRunnable() {
			public void run() {
				try {
					Connection connection = ModReq.getPlugin().getSqlHandler().open();
					if (connection == null) {
						ModReq.getPlugin().sendMsg(player, "error.DATABASE-ERROR");
						return;
					}

					PreparedStatement pStatement;
					if (player.hasPermission("modreq.admin")) {
						pStatement = connection.prepareStatement("SELECT id,uuid,request,timestamp,claimed,elevated FROM modreq WHERE done='0' AND request LIKE ?");
					} else {
						pStatement = connection.prepareStatement("SELECT id,uuid,request,timestamp,claimed,elevated FROM modreq WHERE done='0' AND request LIKE ?");
					}

					pStatement.setString(1, "%" + criteria.trim() + "%");
					ResultSet sqlres = pStatement.executeQuery();
					if (!sqlres.next()) {
						sqlres.close();
						pStatement.close();
						ModReq.getPlugin().sendMsg(player, "mod.check.NO-MODREQS");
					} else {
						ArrayList requests = new ArrayList();

						while(!sqlres.isAfterLast()) {
							requests.add(new Request(sqlres.getInt(1), sqlres.getString(2), sqlres.getString(3), sqlres.getLong(4), "", 0, 0, 0, sqlres.getString(5), "", "", 0L, 0, sqlres.getInt(6)));
							sqlres.next();
						}

						sqlres.close();
						pStatement.close();
						player.sendMessage(ModReq.getPlugin().getLanguageFile().getLangString("mod.check.1").replaceAll("%count", "" + requests.size()));

						for(int ix = 0; ix < requests.size() && ix < requests.size(); ++ix) {
							OfflinePlayer requestUser = null;
							OfflinePlayer claimedUser = null;
							requestUser = ModReq.getPlugin().getOfflinePlayer(((Request)requests.get(ix)).getUuid());
							if (!((Request)requests.get(ix)).getClaimed().equals("")) {
								claimedUser = ModReq.getPlugin().getOfflinePlayer(((Request)requests.get(ix)).getClaimed());
							}

							String status = "";
							if (((Request)requests.get(ix)).getClaimed().equals("")) {
								status = status + ModReq.getPlugin().getLanguageFile().getLangString("general.OPEN");
							} else if (claimedUser.getName() != null) {
								status = status + "§a" + claimedUser.getName();
							} else {
								status = status + "§aunknown";
							}

							if (((Request)requests.get(ix)).getElevated() != 0) {
								status = status + " " + ModReq.getPlugin().getLanguageFile().getLangString("general.ELEVATED");
							}

							pStatement = connection.prepareStatement("SELECT COUNT(id) FROM modreq_notes WHERE modreq_id=?");
							pStatement.setInt(1, ((Request)requests.get(ix)).getId());
							sqlres = pStatement.executeQuery();
							if (sqlres.next() && sqlres.getInt(1) > 0) {
								status = status + " " + ModReq.getPlugin().getLanguageFile().getLangString("general.NOTES");
							}

							sqlres.close();
							pStatement.close();
							String username = "";
							if (requestUser.getName() != null) {
								if (requestUser.isOnline()) {
									username = username + "§a" + requestUser.getName();
								} else {
									username = username + "§c" + requestUser.getName();
								}
							} else {
								username = username + "§cunknown";
							}

							String timestamp_formatted = ModReq.getPlugin().getFormat().format(((Request)requests.get(ix)).getTimestamp());
							player.sendMessage(ModReq.getPlugin().getLanguageFile().getLangString("mod.check.2").replaceAll("%id", "" + ((Request)requests.get(ix)).getId()).replaceAll("%status", status).replaceAll("%date", timestamp_formatted).replaceAll("%player", username));
							player.sendMessage(ModReq.getPlugin().getLanguageFile().getLangString("mod.check.3").replaceAll("%msg", ((Request)requests.get(ix)).getRequest()));
						}

						player.sendMessage(ModReq.getPlugin().getLanguageFile().getLangString("mod.check.1").replaceAll("%count", "" + requests.size()));
					}

					connection.close();
				} catch (SQLException var12) {
					var12.printStackTrace();
					ModReq.getPlugin().sendMsg(player, "error.DATABASE-ERROR");
				}

			}
		};
		runnable.runTaskAsynchronously(ModReq.getPlugin());
	}
}

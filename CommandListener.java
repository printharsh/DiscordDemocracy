import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;

import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.PrivateChannel;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class CommandListener extends ListenerAdapter {
	String commandsChannel = Main.commandsChannel;
	String prefix = Main.commandPrefix;

	public void onMessageReceived(MessageReceivedEvent event) {

		// If message was sent in a guild text channel
		if (event.isFromType(ChannelType.TEXT)) {
			Message m = event.getMessage();
			String message = m.getRawContent();

			if (message.startsWith(prefix)) {
				// PERFORM COMMANDS

				if (message.startsWith("help", 1)) {
					// HELP PERFORM
					help(event);
				} else if (message.startsWith("vote", 1)) {
					// ALLOWS SOMEBODY TO VOTE
					vote(event);

				}

			}

		} else if (event.isFromType(ChannelType.PRIVATE)) {
			// Came from private channel so check potential votes and add
			// question if user starts message with q
			if (!event.getAuthor().isBot()) {

				if (isRoleAdding(event.getAuthor().getIdLong())) {
					// If they are addingRoles then...
					if (event.getMessage().getContent().equals("done")) {
						// IF they said done then change roleAdding
						try {
							changeRoleAdding(event.getAuthor().getIdLong());
						} catch (SQLException e) {
							e.printStackTrace();
						}
					} else {
						// Otherwise addRole...
						try {
							addRole(event.getAuthor().getIdLong(), event.getMessage().getContent());
						} catch (SQLException e) {
							e.printStackTrace();
						}
					}
				}

				else if (event.getMessage().getContent().startsWith("drafts")) {
					System.out.println("yo");
					getDraftVotes(event);

				}

				else if (event.getMessage().getContent().startsWith("q ")) {
					String[] args = event.getMessage().getContent().split(" ");
					if (args.length >= 2) {
						// SEARCH FOR TOPIC...
						String toCheck = args[1];
						// String sql = "SELECT CreatorID, GuildID, GuildName,
						// VoteID, Topic, MultipleVotes, PublicResults,
						// SpecificRoleOnly, Roles, Options, AddingRoles,
						// AddingOptions "
						// + "FROM potentialvotes WHERE (CreatorID = ? AND Topic
						// = ?)";
						
						
						
						long test = 144277213909352449L;
						
						String sql = "" + "SELECT SpecificRoleOnly, PublicResults FROM potentialvotes WHERE CreatorID = " + test ;

						try {
							PreparedStatement pstmt = Main.db.prepareStatement(sql);
							// pstmt.setLong(1, event.getAuthor().getIdLong());
							// pstmt.setString(2, toCheck);

							ResultSet rs = pstmt.executeQuery();

							// CHECKS if row exists
							try {
								rs.next();
								System.out.println("in here");

									System.out.println(rs.getBoolean("SpecificRoleOnly"));
									System.out.println(rs.getBoolean("PublicResults"));

								if (rs.getBoolean(8)) {
									// IF ROLE ADDING IS ENABLED SETS THEM
									// TO
									// ROLE ADDING MODE
									System.out.println("changing");
									changeRoleAdding(event.getAuthor().getIdLong());
								}
							} catch (SQLException e) {
								event.getPrivateChannel()
										.sendMessage("There is no such vote with the topic of " + toCheck).queue();
								getDraftVotes(event);
								System.out.println(e);
							}

						} catch (SQLException e2) {
							// TODO Auto-generated catch block
							e2.printStackTrace();
						}

					} else {
						// TELL EM ONLY 2 arguments
						event.getPrivateChannel().sendMessage("The proper usage is q Topic!").queue();
						getDraftVotes(event);
					}
				} else {
					System.out.println("improper command");
				}
			}
		}
	}

	private void addRole(long idLong, String role) throws SQLException {
		// First queries for roles
		String roles = "";

		String sql = "SELECT Roles " + "FROM potentialvotes WHERE CreatorID = ? AND AddingRoles = ?";
		try {
			PreparedStatement pstmt = Main.db.prepareStatement(sql);
			pstmt.setLong(1, idLong);
			pstmt.setBoolean(2, true);

			ResultSet rs = pstmt.executeQuery();

			roles = rs.getString(1);

		} catch (Exception e) {

		}

		if (roles.equals("")) {
			roles = role;
		} else {
			roles = roles + "," + role;
		}

		// NOW adds roles
		String changeAddingRoles = "UPDATE potentialvotes SET Roles=? WHERE CreatorID=? AND AddingRoles=?";
		PreparedStatement statement = Main.db.prepareStatement(changeAddingRoles);
		statement.setString(1, roles);
		statement.setLong(2, idLong);
		statement.setBoolean(3, true);
		statement.executeUpdate();
	}

	private boolean isRoleAdding(long idLong) {
		String sql = "SELECT CreatorID, AddingRoles " + "FROM potentialvotes WHERE CreatorID = ? AND AddingRoles = ?";
		try {
			PreparedStatement pstmt = Main.db.prepareStatement(sql);
			pstmt.setLong(1, idLong);
			pstmt.setBoolean(2, true);

			ResultSet rs = pstmt.executeQuery();

			if (!rs.next()) {
				return false;
			}

		} catch (Exception e) {

		}
		return true;
	}

	private void changeRoleAdding(long idLong) throws SQLException {
		if (isRoleAdding(idLong)) {
			// If roles need to be added
			String changeAddingRoles = "UPDATE potentialvotes SET AddingRoles=? WHERE CreatorID=? AND AddingRoles=?";
			PreparedStatement statement = Main.db.prepareStatement(changeAddingRoles);
			statement.setBoolean(1, false);
			statement.setLong(2, idLong);
			statement.setBoolean(3, true);
			statement.executeUpdate();
		} else if (!isRoleAdding(idLong)) {
			String changeAddingRoles = "UPDATE potentialvotes SET AddingRoles=? WHERE CreatorID=? AND AddingRoles=?";
			PreparedStatement statement = Main.db.prepareStatement(changeAddingRoles);
			statement.setBoolean(1, true);
			statement.setLong(2, idLong);
			statement.setBoolean(3, false);
			statement.executeUpdate();
		}
	}

	private void getDraftVotes(MessageReceivedEvent event) {
		String sql = "SELECT CreatorID, GuildID, GuildName, VoteID, Topic, MultipleVotes, PublicResults, SpecificRoleOnly, Roles "
				+ "FROM potentialvotes WHERE CreatorID = ?";

		try {
			PreparedStatement pstmt = Main.db.prepareStatement(sql);
			pstmt.setLong(1, event.getAuthor().getIdLong());
			;

			ResultSet rs = pstmt.executeQuery();
			// NOW to get the row
			String toReturn = "```These are your draft votes: \nGuild Name \tTopic \tMultiple Votes \tPublic Results \tSpecific Roles \tRoles \n";
			while (rs.next()) {
				toReturn = toReturn + rs.getString(3) + "\t" + rs.getString(5) + "\t" + rs.getString(6) + "\t"
						+ rs.getString(7) + "\t" + rs.getString(8) + "\t" + rs.getString(9) + "\n";
			}

			event.getPrivateChannel().sendMessage(toReturn + "```").complete();
			event.getPrivateChannel().sendMessage("You can tell me \"q 'Topic'\" to finish creating your poll.")
					.queue();
		} catch (SQLException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
	}

	private void help(MessageReceivedEvent e) {
		TextChannel tc = e.getTextChannel();
		tc.sendMessage(e.getAuthor().getAsMention() + " the help information has been private messaged to you!")
				.complete();

		User u = e.getAuthor();
		PrivateChannel p = u.openPrivateChannel().complete();

		String msg = "__**[Discord Democracy: Help]**__ \n";
		msg = msg + "For this server, these are the following commands: \n";
		msg = msg + "\t" + prefix
				+ "**vote start {topicName} {public/private (whether you can check the results)} ** - Creates a new topic to be voted on. Asks for options after. \n";
		msg = msg + "\t" + prefix
				+ "**vote on {topicName} {option (- to abstain)} ** - Allows a user to vote or change their vote. \n";
		msg = msg + "\t" + prefix + "**vote close ** - Ends a vote for a topic and returns the results";

		p.sendMessage(msg).queue();

	}

	private void vote(MessageReceivedEvent e) {
		String[] args = e.getMessage().getContent().split(" ");
		System.out.println(Arrays.toString(args));
		TextChannel tc = e.getTextChannel();

		if (args.length >= 2) {

			if (args[1].equals("start")) {

				String helpMessage = e.getAuthor().getAsMention() + " the proper usage is: {} = optional \n " + prefix
						+ "vote start (TopicName) {private} {roleLocked} {allowMultiVote}";
				int optionNumber = 0;
				if (args.length < 3) {
					tc.sendMessage(helpMessage).queue();
				} else if (args.length <= 6) {

					String topicName = args[2];
					HashMap<String, Boolean> flags = new HashMap<String, Boolean>();
					flags.put("private", false);
					flags.put("roleLocked", false);
					flags.put("allowMultiVote", false);

					if (args.length == 4) {
						// WHEN 5 argument length
						if (args[3].equals("private")) {
							flags.put("private", true);
						} else if (args[3].equals("roleLocked")) {
							flags.put("roleLocked", true);
						} else if (args[3].equals("allowMultiVote")) {
							flags.put("allowMultiVote", true);
						} else {
							tc.sendMessage(helpMessage).queue();
							return;
						}
					} else if (args.length == 5) {

						if (args[3].equals(args[4])) {
							tc.sendMessage(helpMessage).queue();
							return;
						}

						if (args[3].equals("private")) {
							flags.put("private", true);
						} else if (args[3].equals("roleLocked")) {
							flags.put("roleLocked", true);
						} else if (args[3].equals("allowMultiVote")) {
							flags.put("allowMultiVote", true);
						} else {
							tc.sendMessage(helpMessage).queue();
							return;
						}

						if (args[4].equals("private")) {
							flags.put("private", true);
						} else if (args[4].equals("roleLocked")) {
							flags.put("roleLocked", true);
						} else if (args[4].equals("allowMultiVote")) {
							flags.put("allowMultiVote", true);
						} else {
							tc.sendMessage(helpMessage).queue();
							return;
						}

					} else if (args.length == 6) {
						if (args[3].equals(args[4]) || args[4].equals(args[5]) || args[3].equals(args[5])) {
							tc.sendMessage(helpMessage).queue();
							return;
						}

						if (args[3].equals("private")) {
							flags.put("private", true);
						} else if (args[3].equals("roleLocked")) {
							flags.put("roleLocked", true);
						} else if (args[3].equals("allowMultiVote")) {
							flags.put("allowMultiVote", true);
						} else {
							tc.sendMessage(helpMessage).queue();
							return;
						}

						if (args[4].equals("private")) {
							flags.put("private", true);
						} else if (args[4].equals("roleLocked")) {
							flags.put("roleLocked", true);
						} else if (args[4].equals("allowMultiVote")) {
							flags.put("allowMultiVote", true);
						} else {
							tc.sendMessage(helpMessage).queue();
							return;
						}

						if (args[5].equals("private")) {
							flags.put("private", true);
						} else if (args[5].equals("roleLocked")) {
							flags.put("roleLocked", true);
						} else if (args[5].equals("allowMultiVote")) {
							flags.put("allowMultiVote", true);
						} else {
							tc.sendMessage(helpMessage).queue();
							return;
						}

					}

					e.getTextChannel().sendMessage(
							e.getAuthor().getAsMention() + ", please private message me this: \n q (Topic) (Question)")
							.queue();

					// SQL statement to add all known information to
					// potential votes
					String sql = "INSERT INTO potentialvotes(CreatorID,GuildID,VoteID,Topic,MultipleVotes,PublicResults,SpecificRoleOnly,GuildName) VALUES(?,?,?,?,?,?,?,?)";
					try {
						PreparedStatement pstmt = Main.db.prepareStatement(sql);
						pstmt.setString(1, e.getAuthor().getId());
						pstmt.setString(2, e.getGuild().getId());
						pstmt.setString(3, "" + findVoteID(e));
						pstmt.setString(4, topicName);
						pstmt.setString(5, flags.get("allowMultiVote").toString());
						pstmt.setString(6, flags.get("private").toString());
						pstmt.setString(7, flags.get("roleLocked").toString());
						pstmt.setString(8, e.getGuild().getName());
						pstmt.executeUpdate();
					} catch (SQLException e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();
					}

					/*
					 * + "	CreatorID integer PRIMARY KEY,\n" +
					 * "	GuildID integer NOT NULL" +
					 * "	VoteID integer NOT NULL" +
					 * "	Topic text NOT NULL," +
					 * "	MultipleVotes boolean default false," +
					 * "	PublicResults boolean default false," +
					 * "	SpecificRoleOnly boolean default false," +
					 * "	name text NOT NULL"
					 */

				}

			} else if (args[1].equals("close")) {
				// CLOSES VOTE
			}

			else if (args[1].equals("on")) {
				tc.sendMessage(e.getAuthor().getAsMention() + " the proper usage is: \n" + prefix
						+ "vote on (TopicName) (option or - to abstain)").queue();
			} else {
				tc.sendMessage(e.getAuthor().getAsMention() + ", that is not a valid command!").queue();
				help(e);
			}
		} else {
			tc.sendMessage(e.getAuthor().getAsMention() + ", that is not a valid command!").queue();
			help(e);
		}

	}

	private int findVoteID(MessageReceivedEvent e) {
		// ALL OF THIS DOWN FINDS VOTE ID!
		String sqlFindGuildRows = "SELECT MAX(VoteID)" + "FROM potentialvotes";

		int voteID = 0;
		try {
			PreparedStatement pstmt = Main.db.prepareStatement(sqlFindGuildRows);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				voteID = rs.getInt(1);
			}
			voteID++;

		} catch (SQLException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}

		return voteID;
	}
}

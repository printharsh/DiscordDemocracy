import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Properties;

import javax.security.auth.login.LoginException;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.exceptions.RateLimitedException;

public class Main {

	// The variable attributes for the bot
	static String token;
	static String commandsChannel;
	static String commandPrefix;
	static Connection db = null;

	// Starts the bot account and connects it with the appropriate bot account
	public static void main(String[] args) {

		// Database initialization for all of the guilds
		try {
			Class.forName("org.sqlite.JDBC");
			db = DriverManager.getConnection("jdbc:sqlite:databaseone.db");
			
			
			Statement stmt = db.createStatement();
			
			//SQL statement to create table, if it doesn't exist for guilds
	        String sql = "CREATE TABLE IF NOT EXISTS activevotes (\n"
	                + "	VoteID integer PRIMARY KEY,\n"
	                + "	GuildID integer NOT NULL,"
	                + "	Topic text NOT NULL,"
	                + "	Options text default 0,"
	                + "	VoterIDs text,"
	                + "	CreatorID integer NOT NULL,"
	                + "	MultipleVotes boolean default false,"
	                + "	PublicResults boolean default false,"
	                + "	SpecificRoleOnly boolean default false,"
	                + "	Roles text"
	                + ");";
	        
	        String sqlTableTwo = "CREATE TABLE IF NOT EXISTS guilds (\n"
	                + "	id integer PRIMARY KEY,\n"
	                + "	name text NOT NULL"
	                + ");";
	        
	        String sqlTableThree = "CREATE TABLE IF NOT EXISTS potentialvotes (\n"
	                + "	CreatorID integer NOT NULL,\n"
	                + "	GuildID integer NOT NULL,"
	                + "	VoteID integer PRIMARY KEY,"
	                + "	Topic text NOT NULL,"
	                + "	MultipleVotes boolean default false,"
	                + "	PublicResults boolean default false,"
	                + "	SpecificRoleOnly boolean default false,"
	                + "	GuildName text NOT NULL,"
	                + "	Roles text,"
	                + "	Options text,"
	                + "	Question text,"
	                + "	AddingQuestion boolean default false,"
	                + "	AddingOptions boolean default false,"
	                + "	AddingRoles boolean default false"
	                + ");";
	        
	        //SQL statement execution
	        stmt.execute(sql);
	        stmt.execute(sqlTableTwo);
	        stmt.execute(sqlTableThree);
	        
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}
		
		System.out.println("Opened database successfully");

		// Loads or creates the properties file
		Properties properties = null;

		try {
			properties = loadProperties();
		} catch (IOException e) {
			System.out.println("Please check your config.properties file!");
		}

		// If the properties file does not have a token then tells user to edit
		// their properties file
		if (properties.getProperty("token").equals("none")) {
			System.out.println("Please edit your properties file!");
			System.exit(0);
		}

		// Sets the attributes according to properties file
		token = properties.getProperty("token");
		commandsChannel = properties.getProperty("commands-channel");
		commandPrefix = properties.getProperty("command-prefix");

		// Creates the new JDA for the bot
		try {
			new JDABuilder(AccountType.BOT).setToken(token).addEventListener(new ReadyListener())
					.addEventListener(new CommandListener()).buildBlocking();
		} catch (LoginException | IllegalArgumentException | InterruptedException | RateLimitedException e) {
			System.out.println(e);
		}

	}

	public static Properties loadProperties() throws IOException {

		// Creates a new properties object
		Properties props = new Properties();

		// Sets the default property values
		props.setProperty("token", "none");
		props.setProperty("commands-channel", "id");
		props.setProperty("command-prefix", ">");

		// Creates the config file if it does not exist, or reads and loads the
		// properties
		File configFile = new File("config.properties");

		if (configFile.exists()) {
			FileReader reader;

			reader = new FileReader(configFile);

			// load the properties file:
			props.load(reader);

		} else {
			writeConfig(props);
		}

		return props;
	}

	public static void writeConfig(Properties props) throws IOException {

		// Writes the properties to the file called config.properties
		FileOutputStream output = new FileOutputStream("config.properties");
		String intro = "[Discord Democracy Bot]: Configuration" + "\n";
		intro = intro + "To reset settings to default delete this file!";
		props.store(output, intro);
	}
}

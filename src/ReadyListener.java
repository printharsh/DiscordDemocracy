import java.sql.PreparedStatement;
import java.sql.SQLException;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class ReadyListener extends ListenerAdapter {

	public void onReady(ReadyEvent e) {
		System.out.println("Discord Democracy is running!");
		System.out.println("Bot token: " + Main.token);
		System.out.println();

		System.out.println("Guilds Connected");
		System.out.println("ID" + "\t" + "Name");

		System.out.println("");
		;

		for (Guild g : e.getJDA().getGuilds()) {
			System.out.println(g.getId() + "\t" + g.getName());

			String sql = "INSERT INTO guilds(id,name) VALUES(?,?)";
			try {
				PreparedStatement pstmt = Main.db.prepareStatement(sql);
				pstmt.setString(1, g.getId());
				pstmt.setString(2, g.getName());
				pstmt.executeUpdate();
			} catch (SQLException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
			
			for (Member m : g.getMembers()) {
				System.out.println(m.getUser().getId());
				//CAN IMPLEMENT USER LIST FOR FURTHER FEATURES
			}

		}

	}

}

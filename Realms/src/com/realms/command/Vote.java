package com.realms.command;

import java.util.HashMap;

import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import com.realms.general.*;
import com.realms.menu.MenuManager;
import com.realms.mode.GameType;

public class Vote extends Command{

	private static String map1;
	private static GameType gt1;
	private static String map2;
	private static GameType gt2;
	public static Scoreboard output;
	
	private static HashMap<String, VoteCast> votes = new HashMap<String, VoteCast>();
	private static int A = 0;
	private static int B = 0;
	private static int R = 0;
	
	private static boolean open = false;
	public static Vote instance = null;
	
	public static enum VoteCast{
		A("Map A"),B("Map B"),R("a Random Map");
		private final String desc;
		private VoteCast(String desc){
			this.desc = desc;
		}public String getDesc(){
			return desc;
		}
	}
	
	public Vote(){
		super("vote");
		Vote.instance = this;
	}
	
	@Override
	public void execute(CommandSender s, String[] args){
		if(!open){
			Broadcast.vote("The vote is not currently open.", s);
			return;
		}
		if(args.length == 1){
			String name;
			if(s instanceof Player) name = ((Player)s).getName();
			else name = "CONSOLE";
			String cast = args[0];
			VoteCast vote = null;
			if(cast.equalsIgnoreCase("A")) vote = VoteCast.A;
			else if(cast.equalsIgnoreCase("B")) vote = VoteCast.B;
			else if(cast.equalsIgnoreCase("R")) vote = VoteCast.R;
			else{
				Broadcast.error("Incorrect usage.", s);
				Broadcast.error("Usage: /vote [A/B/R]", s);
				return;
			}
			if(votes.containsKey(name)){
				VoteCast oldVote = votes.get(name);
				uncastVote(oldVote);
			}castVote(vote);
			votes.put(name, vote);
			updatePlayerReadout();
			Broadcast.vote("You have voted for " + vote.toString(), s);
		}else{
			Broadcast.error("Incorrect usage.", s);
			Broadcast.error("Usage: /vote [A/B/R]", s);
		}
	}
	
	public static void open(String mapA, GameType gtA, String mapB, GameType gtB){
		map1 = mapA;
		gt1 = gtA;
		map2 = mapB;
		gt2 = gtB;
		A = 0; B = 0; R = 0;
		MenuManager.createVoteMenu(gtA.toString() + " on " + mapA, gtB.toString() + " on " + mapB);
		open = true;
		output = Bukkit.getScoreboardManager().getNewScoreboard();
		Objective obj = output.registerNewObjective("Map Vote", "dummy");
		obj.setDisplaySlot(DisplaySlot.SIDEBAR);
		obj.setDisplayName("Map Vote");
	}
	
	public static void broadcastVoteReadout(int seconds){
		Broadcast.vote("Vote for the next map is open for " + seconds + " seconds!");
		Broadcast.vote(ChatColor.DARK_RED + "Map A:~ " + gt1.getFullName() + ChatColor.DARK_GRAY + " on ~" + map1);
		Broadcast.vote(ChatColor.DARK_RED + "Map B:~ " + gt2.getFullName() + ChatColor.DARK_GRAY + " on ~" + map2);
		Broadcast.vote(ChatColor.DARK_RED + "Random" + ChatColor.RED + " (R)");
		Broadcast.vote("Use the " + ChatColor.YELLOW + "Vote Item~ to cast your vote!");
		Broadcast.vote("Current results are A:" + A + "  B:" + B + " R:" + R);
	}
	
	public static void castVote(VoteCast vote){
		if(vote == VoteCast.A) A+=1;
		else if(vote == VoteCast.B) B+=1;
		else if(vote == VoteCast.R) R+=1;
		updatePlayerReadout();
	}
	
	public static void uncastVote(VoteCast vote){
		if(vote == VoteCast.A) A-=1;
		else if(vote == VoteCast.B) B-=1;
		else if(vote == VoteCast.R) R-=1;
		updatePlayerReadout();
	}
	
	public static VoteCast closePolls(){
		open = false;
		Broadcast.vote("Voting for next map is now closed.");
		VoteCast win;
		if(A >= B && A >= R) win = VoteCast.A;
		else if(B > A && B >= R) win = VoteCast.B;
		else win = VoteCast.R;
		HotbarManager.clearAll();
		return win;
	}
	
	public static void updatePlayerReadout(){
		output.getObjective("Map Vote").unregister();
		Objective obj = output.registerNewObjective("Map Vote", "dummy");
		obj.setDisplayName("Vote for Next Map");
		obj.setDisplaySlot(DisplaySlot.SIDEBAR);
		Score a = obj.getScore(Bukkit.getOfflinePlayer(ChatColor.GRAY + "Map A"));
		a.setScore(A + 1);
		Score b = obj.getScore(Bukkit.getOfflinePlayer(ChatColor.GRAY + "Map B"));
		b.setScore(B + 1);
		Score r = obj.getScore(Bukkit.getOfflinePlayer(ChatColor.GRAY + "Random Map"));
		r.setScore(R + 1);
	}
	
}
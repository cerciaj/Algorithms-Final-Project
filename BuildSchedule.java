import java.util.*;
/**
 * This program finds the best schedule for any team in the major leagues 
 * It follows all of the parameters as outlined by the MLBPA
 * It uses kruskalls algorithm to determine where the next closest city is to play a game
 * Thus all road trips are maximized to have a minimum cost 
 * All home games cost the same regardless of where they fall in the schedule
 * @author Andrei Cerci, Karl Kiesel
 *
 */
public class BuildSchedule {
	/*
	 * These are the global 
	 * cost to fly is .64 cents per mile
	 * cost to drive is 1.49 cents per mile
	 * MLB is the set of all MLB teams with their data entered 
	 * This is basically a database  
	 */
	private static final double costToFly = .64;
	private static final double costToDrive = 1.49;
	private static final Series[] MLB = {
			new Series("Yankees", "New York", "ALE", "AL", 40.77, 73.98 ),  
			new Series("Red Sox", "Boston", "ALE", "AL", 42.37, 71.03),  
			new Series("Rays", "Tampa Bay", "ALE", "AL", 27.97, 82.53), 
			new Series("Blue Jays", "Toronto", "ALE", "AL", 43.65, 79.38),
			new Series("Orioles", "Baltimore", "ALE", "AL", 39.18, 76.67), 
			new Series("Twins", "Minneapolis", "ALC", "AL", 44.83, 93.47),
			new Series("Indians", "Cleveland", "ALC", "AL", 41.52, 81.68),
			new Series("White Sox", "Chicago", "ALC", "AL", 41.90, 87.65),
			new Series("Royals", "Kansas City", "ALC", "AL", 39.32, 94.72),
			new Series("Tigers", "Detroit", "ALC", "AL", 42.42, 83.02),
			new Series("A's", "Oakland", "ALW", "AL", 37.73, 122.22),
			new Series("Astros", "Houston", "ALW", "AL", 29.97, 95.35),
			new Series("Mariners", "Seattle", "ALW", "AL", 47.45, 122.30),
			new Series("Angels", "Los Angeles", "ALW", "AL", 33.39, 118.40),
			new Series("Rangers", "Arlington", "ALW", "AL", 32.82, 97.35),
			new Series("Marlins", "Miami", "NLE", "NL", 25.92, 80.28),
			new Series("Mets", "New York", "NLE", "NL", 40.77, 73.98),
			new Series("Braves", "Atlanta", "NLE", "NL", 33.65, 84.42),
			new Series("Phillies", "Philadelphia", "NLE", "NL", 39.88, 75.25),
			new Series("Nationals", "D.C", "NLE", "NL", 38.85, 77.04),
			new Series("Cardinals", "St. Louis", "NLC", "NL", 38.75, 90.37),
			new Series("Cubs", "Chicago", "NLC", "NL", 41.90, 87.65), 
			new Series("Reds", "Cincinnati", "NCL", "NL", 39.05, 84.67),
			new Series("Brewers", "Milwaukee", "NLC", "NL", 42.95, 87.90),
			new Series("Pirates", "Pittsburgh", "NLC", "NL", 40.35, 79.93),
			new Series("Dodgers", "Los Angeles", "NLW", "NL", 33.39, 118.40), 
			new Series("Padres", "San Diego", "NLW", "NL", 32.73, 117.17),
			new Series("Giants", "San Francisco", "NLW", "NL", 37.75, 122.68),
			new Series("Rockies", "Denver", "NLW", "NL", 39.75, 104.87),
			new Series("Diamond Backs", "Phoenix", "NLW", "NL", 33.43, 112.02),
	};
	/**
	 * This method builds an array of Series that will be played according to the 
	 * rules of the MLB
	 * @param T Series we are building the schedule for 
	 * @return An array of Series in the schedule
	 */
	public static Series[] buildTBP(Series T) {
		Random rand = new Random();
		ArrayList<Series> tbp = new ArrayList<>();
		// 54 Series 
		// 24 division games each team in the division should be played 6 times
		int k = 0;
		while (k < 24) {
			for (int p = 0; p < 30; p++)
			if (MLB[p].getDiv().equals(T.getDiv()) 
					&& !MLB[p].getName().equals(T.getName())) {
				for (int l = 0; l < 6; l++) {
					tbp.add(MLB[p]);
					k++;
				}
			}
		}
		// 20 inter-division series not including division games 
		// Play each team in the league 3 times
		int i = 0;
		while (i < 20) {
			int series = rand.nextInt(30);
			if (!MLB[series].getDiv().equals(T.getDiv()) 
					&& MLB[series].getLeague().equals(T.getLeague())
					&& countGamesVs(MLB[series], tbp) < 4) {
				tbp.add(MLB[series]);
				i++;
			}
		}
		// 10 inter-league games 
		// can't play one team more than 1 time
		int j = 0;
		while (j < 10) {
			int series = rand.nextInt(30);
			if (MLB[series].getLeague().equals(T.getLeague()) == false
					&& !tbp.contains(MLB[series])) {
				tbp.add(MLB[series]);
				j++;
			}
		}
		// create an array
		Series[] finalTbp = new Series[54];
		// Put the array list in the array
		for (int z = 0; z < 54; z++)
			finalTbp[z] = tbp.get(z);
		// Calculate the cost of each series 
		calculateCost(finalTbp, T);
		// return the updated array
		return finalTbp;
	}
	
	public static int countGamesVs(Series T, ArrayList<Series> S) {
		int count = 0;
		for (Series G : S) {
			if(T.equals(G))
				count++;
		}
		return count;
	}
	/**
	 * This method sets the cost map for the entire schedule 
	 * @param tbp Take in the 
	 * @param thisSeries The series being calculated against
	 */
	public static void calculateCost(Series[] tbp, Series thisSeries) {
		// for every entry in the teams to be played list 
		for (Series T : tbp) {
			// find the miles 
			double dist = findMiles(thisSeries, T);
			double cost;
			// if less than 350 miles use driving cost else use flying
			if (dist < 350) {
				cost = dist * costToDrive;
			} else {
				cost = dist * costToFly;
			}
			// set the series cost to cost
			T.setCost(cost);
		}
	}
	
	/**
	 * Helper method that calculates the cost between two cities rather than
	 * set the cost for the series variable
	 * @param thatSeries series 1
	 * @param thisSeries series 2
	 * @return the final cost
	 */
	public static double calculateCost(Series thatSeries, Series thisSeries) {
		double dist = findMiles(thisSeries, thatSeries);
		if (dist < 350) {
			return dist * costToDrive;
		} else {
			return dist * costToFly;
		}
	}
	/**
	 * Use distance formula to calculate linear distance 
	 * @param thisT city 1
	 * @param thatT city 2
	 * @return the distance in miles
	 */
	public static double findMiles(Series thisT, Series thatT) {
		// get the latitude and longitude and convert to radians
		double thisLon = Math.toRadians(thisT.getLon());
		double thisLat = Math.toRadians(thisT.getLat());
		double thatLon = Math.toRadians(thatT.getLon());
		double thatLat = Math.toRadians(thatT.getLat());
		// find the difference 
		double diffLon = thatLon - thisLon;
		double diffLat = thatLat - thisLat;
		// run distance formula
		double a = Math.pow(Math.sin(diffLat / 2), 2) 
                + Math.cos(thisLat) * Math.cos(thatLat) 
                * Math.pow(Math.sin(diffLon / 2),2); 
		double c = 2 * Math.asin(Math.sqrt(a));
		double rad = 3956;
		double dist = c * rad;
		// return distance
		return dist;
	}
	// build array of nearest cities for each team
	public static Series[] setNearestDist(Series S) {
		// length 30 for all teams
		Series[] costs = new Series[30];
		// for all the teams add their list of cities
		for (int i = 0; i < 30; i++) {
			costs[i] = MLB[i];
			costs[i].setCost(calculateCost(S, MLB[i]));
		}
		// sort from cheapest to most expensive 
		Arrays.sort(costs);
		return costs;
	}
	/**
	 * Helper method that runs through every city in the MLB
	 */
	public static void setList() {
		for(Series S : MLB) {
			S.setClosestCities(setNearestDist(S));
		}
	}
	/**
	 * finds the best schedule using a modified Kruskalls 
	 * Uses the union of closest teams, teams, to be played, and MLB to 
	 * add the next game to the list 
	 * @param S is the teams to be played 
	 * @param T Team for which we are building the schedule 
	 * @return Return the best possible schedule 
	 */
	public static Series[] findBestSchedule(Series[] S, Series T) {
		// the first source is the first city
		Series src = T;
		ArrayList<Series> tbp = new ArrayList<>();
		// sort the tbp
		Arrays.sort(S);
		Series[] best = new Series[54];
		// add to array list for convenience 
		for(Series tmp : S) {
			tbp.add(tmp);
		}
		// the first series will always be the closest team to the home team
		best[0] = tbp.get(0);
		src = tbp.get(0);
		// starting at 1
		int i = 1;
		// while we are less than 54 games
		while(!tbp.isEmpty()) {
			// for all the teams in MLB
			for (int j = 0; j < 30; j++) {
				// once schedule is full don't add any more games 
				if(best[53] != null)
					return best;
				/*
				 * if the city that is closest to the source and is also 
				 * in tbp then add it to the schedule and make it the new source 
				 * if not keep looking for the next closest city
				 * after the series is added remove it from the list of tbp
				 *  you can't play the same team twice in a row  
				 */
				else if (tbp.contains(src.getClosestCities()[j]) 
						&& !best[i-1].getName().equals(src.getClosestCities()[j].getName())) {
					best[i] = src.getClosestCities()[j];
					src = src.getClosestCities()[j];
					tbp.remove(src.getClosestCities()[j]);
					i++;
				}
			}		
		}
		return best;
	}
	/**
	 * This will generate all the individual games 
	 * @param T team 
	 * @return all 162 games
	 */
	public static Game[] generateGames(Series T) {
		// random number 
		Random rand = new Random();
		// initialize all the closest teams lists
		setList();
		// build the tbp list
		Series[] tbp = buildTBP(T);
		// find the best schedule 
		Series[] best = findBestSchedule(tbp, T);
		// build all the games
		Game[] gameSchedule = new Game[best.length * 3];
		int added = 0;
		// for all the series in the best schedule
		for (Series S : best) {
			// random home vs away
			boolean homeOrAway = rand.nextBoolean();
			// random time 
			int time = 1 + rand.nextInt(10);
			// all series are 3 games long 
			for (int i = 0; i < 3; i ++) {
				// if its away then its team t at ... else its ... at home team
				if (homeOrAway == true) {
					gameSchedule[added] = new Game(T.getName(), S.getName(), time);
					added++;
				} else {
					gameSchedule[added] = new Game(S.getName(), T.getName(), time);
					added++;
				}
			}	
		}
		return gameSchedule;
	}
	/**
	 * Main method 
	 * @param args command line and path
	 */
	public static void main(String[] args) {
		// start the season
		// timer
		final long startTime = System.currentTimeMillis();
		// counter for series number
		int count = 0;
		System.out.println("Start");
		// test team 
		Series boston = MLB[1];
		System.out.println("Generating schedule for " + boston.getName());
		// generate all the games
		Game[] schedule = generateGames(boston);
		for (Game G : schedule) {
			System.out.println(G.toString());
			count++;
		}
		System.out.println("End");
		System.out.println("Number of series: " + count/3);
		final long endTime = System.currentTimeMillis();
		System.out.println("Total execution time: " + (endTime - startTime)/1000.0 + " seconds");
	}
}
/**
 * {@inheritDoc}
 * Subclass for structure games
 * @author Andrei Cerci 
 *
 */
class Game {
	private String home;
	private String away;
	private int time;
	/**
	 * Constructor for games have a home an away and time
	 * @param home Home team
	 * @param away Away Team
	 * @param time Start time
	 */
	public Game(String home, String away, int time) {
		super();
		this.home = home;
		this.away = away;
		this.time = time;
	}

	public String getHome() {
		return home;
	}

	public void setHome(String home) {
		this.home = home;
	}

	public String getAway() {
		return away;
	}

	public void setAway(String away) {
		this.away = away;
	}

	public int getTime() {
		return time;
	}

	public void setTime(int time) {
		this.time = time;
	}
	/**
	 * Overrides object toString() 
	 */
	@Override
	public String toString() {
		return "Game [" + away + " at " + home + ", first pitch " + time + " pm]";
	}
	
}
/**
 * {@inheritDoc}
 * Structure for a series that will be compared by cost implements comparable
 * @author Andrei Cerci
 *
 */
class Series implements Comparable<Series> {
	private String name;
	private String city;
	private String div;
	private String league;
	private double lat;
	private double lon;
	private double cost;
	private boolean homeOrAway;
	private Series[] closestCities;
	/**
	 * Blank constructor 
	 */
	public Series() {}
	/**
	 * Primary constructor for a series 
	 * @param name Name of team
	 * @param city City of team
	 * @param div Division of team
	 * @param league League of team
	 * @param lat Latitude coordinate 
	 * @param lon Longitude coordinate
	 */
	public Series(String name, String city, String div, String league, double lat, double lon) {
		this.name = name;
		this.city = city;
		this.div = div;
		this.league = league;
		this.lat = lat;
		this.lon = lon;
	}

	public double getLat() {
		return lat;
	}

	public void setLat(double lat) {
		this.lat = lat;
	}

	public double getLon() {
		return lon;
	}

	public void setLon(double lon) {
		this.lon = lon;
	}

	public String getLeague() {
		return league;
	}

	public void setLeague(String league) {
		this.league = league;
	}

	public String getDiv() {
		return div;
	}

	public void setDiv(String div) {
		this.div = div;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public double getCost() {
		return cost;
	}

	public void setCost(double cost) {
		this.cost = cost;
	}

	public Series[] getClosestCities() {
		return closestCities;
	}

	public void setClosestCities(Series[] closestCities) {
		this.closestCities = closestCities;
	}
	/**
	 *  To string that overrides object for a series prints the name, city, and cost;
	 */
	@Override
	public String toString() {
		return "Series [name=" + name + ", city=" + city + ", cost=" + cost + "]";
	}
	public int compareTo(Series o1, Series o2) {
		if (o1.getCost() > o2.getCost())
			return 1;
		else if (o1.getCost() < o2.getCost())
			return -1;
		else
			return 0;
	}
	/**
	 * Overrides object comparable. Compares two series by cost first 
	 */
	@Override
	public int compareTo(Series o) {
		if (this.getCost() > o.getCost())
			return 1;
		else if (this.getCost() < o.getCost())
			return -1;
		else
			return 0;
	}
	/**
	 * Overrides equals by comparing the elements of the object 
	 */
	@Override
	public boolean equals(Object obj) {
		Series other = (Series) obj;
		if (city == null) {
			if (other.city != null)
				return false;
		} else if (!city.equals(other.city))
			return false;
		if (div == null) {
			if (other.div != null)
				return false;
		} else if (!div.equals(other.div))
			return false;
		if (homeOrAway != other.homeOrAway)
			return false;
		if (Double.doubleToLongBits(lat) != Double.doubleToLongBits(other.lat))
			return false;
		if (league == null) {
			if (other.league != null)
				return false;
		} else if (!league.equals(other.league))
			return false;
		if (Double.doubleToLongBits(lon) != Double.doubleToLongBits(other.lon))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
}


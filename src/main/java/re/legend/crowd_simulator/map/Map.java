package re.legend.crowd_simulator.map;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeMap;
import java.util.UUID;

import com.badlogic.gdx.math.Vector2;
import com.google.common.graph.MutableGraph;

import re.legend.crowd_simulator.entities.SimulationEntity;
import re.legend.crowd_simulator.entities.bodies.AgentBody;
import re.legend.crowd_simulator.entities.gameobjects.Bomb;
import re.legend.crowd_simulator.entities.gameobjects.Shop;
import re.legend.crowd_simulator.entities.gameobjects.Wall;
import re.legend.crowd_simulator.pathfinding.AStarNode;

public class Map {

	// Size of each cell of the map
	public static final int CELL_SIZE = 16;
	
	// Map size
	public static final int MAP_SIZE = 560;
	
	// TreeMap containing the bodies on the map
	private TreeMap<UUID, AgentBody> bodies;

	// List of walls on the map
	private List<Wall> walls;

	// List of waypoints on the map
	private MutableGraph<AStarNode> waypoints;

	// List of shops on the map
	private List<Shop> shops;
	
	// The bomb
	private Bomb bomb;
	
	// List of exits on the map
	private List<Vector2> exits;
	
	/**
	 * Default constructor
	 */
	public Map() {
		this.bodies = new TreeMap<>();
		this.walls = new ArrayList<>();
		this.shops = new ArrayList<>();
	}

	/**
	 * Create a body
	 * 
	 * @param bodyType
	 * @param agentUUID
	 * @return
	 * @throws InvocationTargetException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 */
	public <T extends AgentBody> T createBody(Class<T> bodyType, UUID agentUUID, float x, float y)
			throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException, SecurityException {
		// Body position
		float orientation = 0;

		// Agent's ID
		UUID id = agentUUID;
		if (agentUUID == null) {
			id = UUID.randomUUID();
		}

		// Create body instance
		Constructor cons = bodyType.getDeclaredConstructor(float.class, float.class, float.class, UUID.class);
		Object body = cons.newInstance(x, y, orientation, id);

		// Put the body into the tree map
		this.bodies.put(id, (AgentBody) body);

		return (T) body;
	}

	public void setWalls(List<Wall> walls) {
		this.walls = walls;
	}

	public void setShops(List<Shop> shops) {
		this.shops = shops;
	}
	
	public Collection<AgentBody> getBodies() {
		return this.bodies.values();
	}

	public void setBodyAt(AgentBody body, float x, float y) {
		body.setPosition(x, y);
	}

	public SimulationEntity getObjectAt(float x, float y) {
		for (Wall wall : walls) {
			if (wall.getPosition().x == x && wall.getPosition().y == y) {
				return wall;
			}
		}
		return null;
	}

	public List<Wall> getWalls() {
		return this.walls;
	}
	
	public List<Shop> getShops() {
		return this.shops;
	}

	public void setWaypoints(MutableGraph<AStarNode> waypoints) {
		this.waypoints = waypoints;
	}
	
	public MutableGraph<AStarNode> getWaypoints() {
		return this.waypoints;
	}

	/**
	 * @return the bomb
	 */
	public Bomb getBomb() {
		return this.bomb;
	}

	/**
	 * @param bomb the bomb to set
	 */
	public void setBomb(Bomb bomb) {
		this.bomb = bomb;
	}
	
	public void setExits(List<Vector2> exits) {
		this.exits = exits;
	}
	
	public List<Vector2> getExits() {
		return this.exits;
	}
	
	public void removeBody(UUID id) {
		this.bodies.remove(id);
	}
}

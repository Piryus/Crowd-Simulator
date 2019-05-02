package re.legend.crowd_simulator.map;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.TreeMap;
import java.util.UUID;

import com.badlogic.gdx.math.Quaternion;

import re.legend.crowd_simulator.bodies.AgentBody;
import re.legend.crowd_simulator.objects.Wall;


public class Map {
	
	Random random;
	// TreeMap containing the bodies on the map
	TreeMap<UUID, AgentBody> bodies;
	// List of walls on the map
	List<Wall> walls;
	
	/**
	 * Default constructor
	 */
	public Map() {
		this.random = new Random();
		this.bodies = new TreeMap<>();
		this.walls = new ArrayList<>();
	}

	/**
	 * Create a body 
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
	public <T extends AgentBody> T createBody(Class<T> bodyType, UUID agentUUID) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		// Body position
		float x = this.random.nextInt(200);
		float y = this.random.nextInt(200);
		Quaternion o = new Quaternion();
		
		// Agent's ID
		UUID id = agentUUID;
		if (agentUUID == null) {
			id = UUID.randomUUID();
		}
		
		// Create body instance
		Constructor cons = bodyType.getDeclaredConstructor(float.class, float.class, Quaternion.class, UUID.class);
		Object body = cons.newInstance(x, y, o, id);

		// Put the body into the tree map
		this.bodies.put(id, (AgentBody) body);

		return (T) body;
	}
	
	public void setWalls(List<Wall> walls) {
		this.walls = walls;
	}
	
	public Collection<AgentBody> getBodies() {
		return this.bodies.values();
	}
	
	public void setBodyAt(AgentBody body, float x, float y) {
		body.setPosition(x, y);
	}
	 
}
package re.legend.crowd_simulator.entities.bodies;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.badlogic.gdx.math.Vector2;

import re.legend.crowd_simulator.entities.SimulationEntity;
import re.legend.crowd_simulator.entities.gameobjects.Shop;
import re.legend.crowd_simulator.frustum.EntityFrustum;
import re.legend.crowd_simulator.influence.Influence;
import re.legend.crowd_simulator.influence.MotionInfluence;
import re.legend.crowd_simulator.pathfinding.Path;

public abstract class AgentBody extends SimulationEntity {

	// Body agent's ID
	private UUID agentId;

	// Manage the linear and angular speed
	private Vector2 linearVelocity;
	private float angularVelocity;

	// Max velocity of the agent
	public static final float MAX_VELOCITY = 10;

	// Max force of the agent
	public static final float MAX_FORCE = 15;

	// Max distance at which the agent can perceive other bodies
	public static final float PERCEPTION_DISTANCE = 10;

	// Distance from the target at which the agent should start slowing down
	public static final float SLOW_DOWN_DISTANCE = 15f;

	// Distance at which it should stop
	public static final float STOP_DISTANCE = 5f;

	// Distance between the ahead vector and another agent at which there should be
	// avoidance forces computed
	public static final float BODY_AHEAD_INTERSECTION_DISTANCE = 10f;

	// Distance between the ahead vector and a wall at which there should be
	// avoidance forces computed
	public static final float WALL_AHEAD_INTERSECTION_DISTANCE = 7f;

	// Distance at which we consider the agent has reached its target
	public static final float REACHED_TARGET_DISTANCE = 20f;

	// Distance at which we consider the agent has reached the shop entrance
	public static final float REACHED_SHOP_ENTRANCE_DISTANCE = 10f;

	// Distance at which we consider the agent has reached the mall exit
	public static final float REACHED_EXIT_DISTANCE = 30f;

	// Coordinates of the target to reach
	private Vector2 target;

	// Desired velocity - force towards the target
	private Vector2 desiredVelocity;

	// Steering force (desired velocity - current velocity)
	private Vector2 steering;

	// Body's perception frustum
	private EntityFrustum frustum;

	// Other bodies perceived by this body
	private List<AgentBody> perceivedBodies;

	// Objects perceived by this body
	private List<SimulationEntity> perceivedObjects;

	// Body's influences
	private List<Influence> influences;

	private Vector2 ahead;
	private Vector2 ahead2;
	private Vector2 avoidance;

	// Current path followed by the agent
	private Path path;

	// The current node the agent is targeting
	private int currentNode;

	// The shop the agent wants to visit
	private Shop visitedShop;

	// The shop entrance that the agent is targetting
	private Vector2 shopEntrance;

	// Time at which the agent has acquired its target while shopping, used for
	// random moves in the shops
	public long shopTargetAcquiredTime;

	// Time at which the agent has started shopping and has entered a shop
	public long shoppingStartedTime;

	// Nearest exit to the agent
	public Vector2 nearestExit;

	/**
	 * Constructor with body's position (two floats) and UUID
	 */
	public AgentBody(float x, float y, float orientation, UUID id) {
		super(x, y, orientation);
		this.agentId = id;
		this.influences = new ArrayList<>();
		this.linearVelocity = new Vector2();
		this.perceivedBodies = new ArrayList<>();
		this.perceivedObjects = new ArrayList<>();
		this.ahead = new Vector2();
		this.ahead2 = new Vector2();
		this.avoidance = new Vector2();
	}

	/**
	 * Constructor with body's position (vector2) and UUID
	 */
	public AgentBody(Vector2 position, float orientation, UUID id) {
		this(position.x, position.y, orientation, id);
	}

	/**
	 * @return the uuid of the agent
	 */
	public UUID getUuid() {
		return this.agentId;
	}

	/**
	 * @return the perception frustum of the body
	 */
	public EntityFrustum getFrustum() {
		return this.frustum;
	}

	/**
	 * Sets which objects and which bodies are perceived by this body
	 * 
	 * @param bodies  the other bodies perceived by the body
	 * @param objects the objects perceived by the body
	 */
	public void setPerceptions(List<AgentBody> bodies, List<SimulationEntity> objects) {
		synchronized (this.perceivedBodies) {
			this.perceivedBodies = bodies;
		}
		synchronized (this.perceivedObjects) {
			this.perceivedObjects = objects;
		}
	}

	/**
	 * @return the objects perceived by the body
	 */
	public List<SimulationEntity> getPerceivedObjects() {
		return this.perceivedObjects;
	}

	/**
	 * @return the bodies perceived by this body
	 */
	public List<AgentBody> getPerceivedBodies() {
		return this.perceivedBodies;
	}

	/**
	 * @param influence the influence to add to the body
	 */
	public void addInfluence(Influence influence) {
		synchronized (this.influences) {
			this.influences.add(influence);
		}
	}

	/**
	 * @return all the influences of the body
	 */
	public List<Influence> getInfluences() {
		return this.influences;
	}

	/**
	 * @return all the motion influences of the body
	 */
	public List<MotionInfluence> getMotionInfluences() {
		List<MotionInfluence> motionInfluences = new ArrayList<>();
		if (this.influences != null && !this.influences.isEmpty()) {
			synchronized (this.influences) {
				for (Influence influence : this.influences) {
					if (influence instanceof MotionInfluence) {
						motionInfluences.add((MotionInfluence) influence);
					}
				}
			}
		}
		return motionInfluences;
	}

	/**
	 * Clear the list of influences
	 */
	public void clearInfluences() {
		this.influences.clear();
	}

	/**
	 * @return the linearVelocity
	 */
	public Vector2 getLinearVelocity() {
		return this.linearVelocity;
	}

	/**
	 * @return the angularVelocity
	 */
	public float getAngularVelocity() {
		return this.angularVelocity;
	}

	/**
	 * @return the target
	 */
	public Vector2 getTarget() {
		return this.target;
	}

	/**
	 * @return the ahead vector
	 */
	public Vector2 getAhead() {
		return this.ahead;
	}

	/**
	 * @return the ahead2 vector
	 */
	public Vector2 getAhead2() {
		return this.ahead2;
	}

	/**
	 * @return the desired velocity of the agent
	 */
	public Vector2 getDesiredVelocity() {
		return this.desiredVelocity;
	}

	/**
	 * @return the avoidance force of the agent
	 */
	public Vector2 getAvoidance() {
		return this.avoidance;
	}

	/**
	 * @return the path followed by the agent
	 */
	public Path getPath() {
		return path;
	}

	/**
	 * @param path the path the agent must follow
	 */
	public void setPath(Path path) {
		this.path = path;
	}

	/**
	 * @param target the target to set
	 */
	public void setTarget(Vector2 target) {
		this.target = target;
	}

	public Shop getVisitedShop() {
		return visitedShop;
	}

	public void setVisitedShop(Shop visitedShop) {
		this.visitedShop = visitedShop;
	}

	public Vector2 getShopEntrance() {
		return shopEntrance;
	}

	public void setShopEntrance(Vector2 shopEntrance) {
		this.shopEntrance = shopEntrance;
	}

	public long getShoppingStartedTime() {
		return shoppingStartedTime;
	}

	public void setShoppingStartedTime(long shoppingStartedTime) {
		this.shoppingStartedTime = shoppingStartedTime;
	}

	public void seek() {
		// Computes the desired velocity towards the target
		this.desiredVelocity = this.target.cpy().sub(this.position);

		// Gets the distance to the target
		float distance = this.desiredVelocity.len();

		// Normalizes and scale to the desired velocity the maximum velocity
		this.desiredVelocity.nor().scl(MAX_VELOCITY);

		// On arrival, slows down the agent
		if (distance <= SLOW_DOWN_DISTANCE) {
			this.desiredVelocity.scl(distance / SLOW_DOWN_DISTANCE);
		} else if (distance <= STOP_DISTANCE) {
			this.desiredVelocity.scl(0);
		}

		// Computes the steering force
		this.steering = this.desiredVelocity.cpy().sub(this.linearVelocity);
		// TODO Make the force depends on the mass (sex dependent ?)
	}

	public void avoidCollisionWithBodies() {
		// The ahead vector is the velocity vector with the PERCEPTION_DISTANCE length
		this.ahead = this.position.cpy().add(this.linearVelocity.cpy().nor().scl(PERCEPTION_DISTANCE));
		this.ahead2 = this.position.cpy().add(this.linearVelocity.cpy().nor().scl(PERCEPTION_DISTANCE * 0.5f));

		// Find the most threatening body's position
		Vector2 bodyToAvoidPosition = findMostThreateningBodyPosition();

		// Computes the avoidance force depending on the position of the most
		// threatening body found
		// If no body was found, the avoidance force is null
		if (bodyToAvoidPosition != null) {
			this.avoidance = this.ahead.cpy().sub(bodyToAvoidPosition).nor().scl(MAX_FORCE);
		} else {
			this.avoidance.scl(0);
		}

		// Adds the avoidance force to the steering
		this.steering.add(this.avoidance);
	}

	// Avoid collision with Walls
	public void avoidCollisionWithWalls() {
		// The ahead vector is the velocity vector with the PERCEPTION_DISTANCE length
		this.ahead = this.position.cpy().add(this.linearVelocity.cpy().nor().scl(PERCEPTION_DISTANCE));
		this.ahead2 = this.position.cpy().add(this.linearVelocity.cpy().nor().scl(PERCEPTION_DISTANCE * 0.5f));

		// Find the most threatening wall position
		Vector2 wallToAvoidPosition = findMostThreateningWall();

		// Computes the avoidance force depending on the position of the closest wall
		// found
		// If no wall was found, the avoidance force is null
		if (wallToAvoidPosition != null) {
			this.avoidance = this.ahead.cpy().sub(wallToAvoidPosition).nor().scl(MAX_FORCE * 3);// More force so the
																								// ahead vector is not
																								// stuck in the wall
		} else {
			this.avoidance.scl(0);
		}

		// Adds the avoidance force to the steering
		this.steering.add(this.avoidance);
	}

	public void computesVelocity() {
		// Computes the new velocity of the agent
		this.linearVelocity.add(this.steering);
	}

	private boolean lineIntersectsBodyCircle(Vector2 bodyPosition) {
		if (Vector2.dst(bodyPosition.x, bodyPosition.y, this.ahead.x, this.ahead.y) <= BODY_AHEAD_INTERSECTION_DISTANCE
				|| Vector2.dst(bodyPosition.x, bodyPosition.y, this.ahead2.x,
						this.ahead2.y) <= BODY_AHEAD_INTERSECTION_DISTANCE
				|| Vector2.dst(bodyPosition.x, bodyPosition.y, this.position.x,
						this.position.y) <= BODY_AHEAD_INTERSECTION_DISTANCE) {
			return true;
		}
		return false;
	}

	private boolean lineIntersectsWallCircle(Vector2 wall) {
		if (Vector2.dst(wall.x + 4, wall.y + 4, this.ahead.x, this.ahead.y) <= WALL_AHEAD_INTERSECTION_DISTANCE
				|| Vector2.dst(wall.x + 8, wall.y + 4, this.ahead.x, this.ahead.y) <= WALL_AHEAD_INTERSECTION_DISTANCE
				|| Vector2.dst(wall.x + 12, wall.y + 4, this.ahead.x, this.ahead.y) <= WALL_AHEAD_INTERSECTION_DISTANCE
				|| Vector2.dst(wall.x + 4, wall.y + 8, this.ahead.x, this.ahead.y) <= WALL_AHEAD_INTERSECTION_DISTANCE
				|| Vector2.dst(wall.x + 8, wall.y + 8, this.ahead.x, this.ahead.y) <= WALL_AHEAD_INTERSECTION_DISTANCE
				|| Vector2.dst(wall.x + 12, wall.y + 8, this.ahead.x, this.ahead.y) <= WALL_AHEAD_INTERSECTION_DISTANCE
				|| Vector2.dst(wall.x + 4, wall.y + 11, this.ahead.x, this.ahead.y) <= WALL_AHEAD_INTERSECTION_DISTANCE
				|| Vector2.dst(wall.x + 8, wall.y + 11, this.ahead.x, this.ahead.y) <= WALL_AHEAD_INTERSECTION_DISTANCE
				|| Vector2.dst(wall.x + 12, wall.y + 11, this.ahead.x, this.ahead.y) <= WALL_AHEAD_INTERSECTION_DISTANCE

				|| Vector2.dst(wall.x + 4, wall.y + 4, this.ahead2.x, this.ahead2.y) <= WALL_AHEAD_INTERSECTION_DISTANCE
				|| Vector2.dst(wall.x + 8, wall.y + 4, this.ahead2.x, this.ahead2.y) <= WALL_AHEAD_INTERSECTION_DISTANCE
				|| Vector2.dst(wall.x + 12, wall.y + 4, this.ahead2.x,
						this.ahead2.y) <= WALL_AHEAD_INTERSECTION_DISTANCE
				|| Vector2.dst(wall.x + 4, wall.y + 8, this.ahead2.x, this.ahead2.y) <= WALL_AHEAD_INTERSECTION_DISTANCE
				|| Vector2.dst(wall.x + 8, wall.y + 8, this.ahead2.x, this.ahead2.y) <= WALL_AHEAD_INTERSECTION_DISTANCE
				|| Vector2.dst(wall.x + 12, wall.y + 8, this.ahead2.x,
						this.ahead2.y) <= WALL_AHEAD_INTERSECTION_DISTANCE
				|| Vector2.dst(wall.x + 4, wall.y + 11, this.ahead2.x,
						this.ahead2.y) <= WALL_AHEAD_INTERSECTION_DISTANCE
				|| Vector2.dst(wall.x + 8, wall.y + 11, this.ahead2.x,
						this.ahead2.y) <= WALL_AHEAD_INTERSECTION_DISTANCE
				|| Vector2.dst(wall.x + 12, wall.y + 11, this.ahead2.x,
						this.ahead2.y) <= WALL_AHEAD_INTERSECTION_DISTANCE)

		{
			return true;
		}
		return false;
	}

	private Vector2 findMostThreateningBodyPosition() {
		Vector2 mostThreateningBodyPos = null;
		synchronized (this.perceivedBodies) {
			if (this.perceivedBodies != null && !this.perceivedBodies.isEmpty()) {
				// Loop through the perceived bodies of the agent
				for (AgentBody body : this.perceivedBodies) {
					// Checks if the agent's ahead vectors collide with the perceived body
					boolean collisionWithBody = lineIntersectsBodyCircle(body.position);
					if (collisionWithBody && (mostThreateningBodyPos == null || Vector2.dst(this.position.x,
							this.position.y, body.position.x, body.position.y) < Vector2.dst(this.position.x,
									this.position.y, mostThreateningBodyPos.x, mostThreateningBodyPos.y))) {
						mostThreateningBodyPos = body.position;
					}
				}
			}
		}
		return mostThreateningBodyPos;
	}

	private Vector2 findMostThreateningWall() {
		Vector2 mostThreateningWallPos = null;
		synchronized (this.perceivedObjects) {
			if (this.perceivedObjects != null && !this.perceivedObjects.isEmpty()) {
				for (SimulationEntity wall : this.perceivedObjects) {
					// Checks if the agent's ahead vectors collide with the perceived body
					boolean collisionWithWall = lineIntersectsWallCircle(wall.getPosition());
					if (collisionWithWall && (mostThreateningWallPos == null || Vector2.dst(this.position.x,
							this.position.y, wall.getPosition().x, wall.getPosition().y) < Vector2.dst(this.position.x,
									this.position.y, mostThreateningWallPos.x, mostThreateningWallPos.y))) {
						mostThreateningWallPos = wall.getPosition();
					}
				}
			}
		}
		return mostThreateningWallPos;
	}

	public boolean hasReachedTarget() {
		return Vector2.dst(this.position.x, this.position.y, this.target.x, this.target.y) < REACHED_TARGET_DISTANCE;
	}

	public void followPath() {
		// Below is the fix to the path bug (agents having thousands of nodes in their
		// path)
		// It removes the duplicated nodes
		// TODO Fix the source of the problem (in the A* algorithm)
		// BEGINNING OF THE FIX
		// System.out.println(this.path.length());
		List<Vector2> tempNodes = new ArrayList<>();
		for (Vector2 node : this.path.getNodes()) {
			boolean inList = false;
			for (Vector2 nodeTemp : tempNodes) {
				if (nodeTemp.x >= node.x - 1 && nodeTemp.y >= node.y - 1 && nodeTemp.x <= node.x + 1
						&& nodeTemp.y <= node.y + 1) {
					inList = true;
					break;
				}
			}
			if (!inList) {
				tempNodes.add(node);
			}
		}
		this.path = new Path(tempNodes);
		// END OF THE FIX

		if (hasReachedTarget() && this.path.length() > this.currentNode + 1) {
			this.currentNode++;
			this.target = this.path.getNode(this.currentNode);
		}
	}

	public boolean hasReachedShopEntrance() {
		return Vector2.dst(this.position.x, this.position.y, this.shopEntrance.x,
				this.shopEntrance.y) < REACHED_SHOP_ENTRANCE_DISTANCE;
	}

	public boolean hasReachedPathLastNode() {
		Vector2 lastNode = this.path.getNode(this.path.getNodes().size() - 1);
		return Vector2.dst(this.position.x, this.position.y, lastNode.x, lastNode.y) < REACHED_TARGET_DISTANCE;
	}

	public boolean hasReachedNearestExit() {
		return Vector2.dst(this.position.x, this.position.y, this.nearestExit.x,
				this.nearestExit.y) < REACHED_EXIT_DISTANCE;
	}

	public void resetCurrentNode() {
		this.currentNode = 0;
	}

	public boolean isInAShop(List<Shop> shops) {
		for (Shop shop : shops) {
			if (shop.getArea().contains(position)) {
				return true;
			}
		}
		return false;
	}
	
	public Shop computeShop(List<Shop> shops) {
		for (Shop shop : shops) {
			if (shop.getArea().contains(position)) {
				return shop;
			}
		}
		return null;
	}
}

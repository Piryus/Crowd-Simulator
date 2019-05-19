package re.legend.crowd_simulator.entities.bodies;
import java.util.UUID;

import com.badlogic.gdx.math.Vector2;


public class KidBody extends AgentBody {

	public KidBody(float x, float y, float o, UUID id) {
		super(x, y, o, id);
	}
	
	public KidBody(Vector2 position, float o, UUID id) {
		super(position, o, id);
	}
	
}

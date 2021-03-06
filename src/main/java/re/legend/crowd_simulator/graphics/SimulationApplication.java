package re.legend.crowd_simulator.graphics;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.util.form.SimpleFormValidator;
import com.kotcrab.vis.ui.widget.LinkLabel;
import com.kotcrab.vis.ui.widget.Menu;
import com.kotcrab.vis.ui.widget.MenuBar;
import com.kotcrab.vis.ui.widget.MenuItem;
import com.kotcrab.vis.ui.widget.VisCheckBox;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.kotcrab.vis.ui.widget.VisValidatableTextField;
import com.kotcrab.vis.ui.widget.VisWindow;

import re.legend.crowd_simulator.entities.SimulationEntity;
import re.legend.crowd_simulator.entities.bodies.AdultBody;
import re.legend.crowd_simulator.entities.bodies.AgentBody;
import re.legend.crowd_simulator.entities.bodies.Sex;
import re.legend.crowd_simulator.entities.gameobjects.Shop;
import re.legend.crowd_simulator.entities.gameobjects.Wall;
import re.legend.crowd_simulator.pathfinding.AStarNode;

public class SimulationApplication extends ApplicationAdapter implements InputProcessor, MapListener {
	private OrthographicCamera camera;

	private TiledMap map;
	private TmxMapLoader loader;
	private OrthogonalTiledMapRenderer renderer;

	// Cursor position on last click
	private Vector2 lastTouch;

	// Agent bodies to render, updated by the update() method
	private List<AgentBody> bodies;

	// Walls list, not used in this class but retrieved
	private List<Wall> walls;

	// Shops list, given to the agents
	private List<Shop> shops;

	// Exits list
	private List<Vector2> exits;

	// Adult bodies textures sprite
	private Texture adultTextures;
	private TextureRegion adultMaleStillLeft;
	private TextureRegion adultMaleStillRight;
	private TextureRegion adultMaleStillFace;
	private TextureRegion adultMaleStillBack;
	private TextureRegion adultFemaleStillLeft;
	private TextureRegion adultFemaleStillRight;
	private TextureRegion adultFemaleStillFace;
	private TextureRegion adultFemaleStillBack;

	// SpriteBatche for the map
	private SpriteBatch spriteBatch;

	// SpriteBatch fixed (for texts)
	SpriteBatch fixedSpriteBatch;

	// Map size
	private int mapWidth;
	private int mapHeight;

	// Timer
	private String strTimer;
	private BitmapFont fontTimer;
	private GlyphLayout timerLayout;

	// Number of agents counter
	private String strCounter;
	private BitmapFont fontCounter;
	private GlyphLayout counterLayout;
	
	// Time since the beginning of the evacuation
	private String strCounterEvacuation;
	private BitmapFont fontCounterEvacuation;
	private GlyphLayout counterEvacuationLayout;
	private long evacuationStartTime;
	private float elapsedTime;

	// Font generator and parameter
	private FreeTypeFontGenerator generator;
	private FreeTypeFontParameter parameter;

	// Shape renderer
	private ShapeRenderer shapeRenderer;

	// Stage
	private Stage stage;
	private VisTextButton startButton;

	// Input multiplexer (used to interact with both the UI and the map)
	private InputMultiplexer inputMultiplexer;

	// Waypoints of the map
	private MutableGraph<AStarNode> waypoints;

	// Booleans to manage the display options
	boolean renderWallsHitboxes;
	boolean renderAgentCircle;
	boolean renderAheadVector;
	boolean renderAhead2Vector;
	boolean renderVelocityVector;
	boolean renderAvoidanceVector;
	boolean renderDesiredVelocityVector;
	boolean renderPaths;
	boolean renderTarget;

	// True if the bomb button has been clicked
	private boolean bombTriggered;
	
	// Spawner parameters
	int spawnerFrequency = 1000;
	int nbAgents = 150;

	// Menu items
	private MenuItem displayOptionsItem;
	private MenuItem spawnerItem;
	private MenuItem triggerBombItem;
	
	@Override
	public void create() {
		// Attributes instantiation
		this.bodies = new ArrayList<>();
		this.lastTouch = new Vector2();
		this.spriteBatch = new SpriteBatch();
		this.fixedSpriteBatch = new SpriteBatch();
		this.walls = new ArrayList<>();
		this.shops = new ArrayList<>();
		this.exits = new ArrayList<>();

		this.shapeRenderer = new ShapeRenderer();
		this.waypoints = GraphBuilder.undirected().build();

		// Loads bodies textures
		this.adultTextures = new Texture("adult_bodies.png");
		this.adultMaleStillLeft = new TextureRegion(this.adultTextures, 0, 0, 16, 16);
		this.adultMaleStillFace = new TextureRegion(this.adultTextures, 16, 0, 16, 16);
		this.adultMaleStillBack = new TextureRegion(this.adultTextures, 32, 0, 16, 16);
		this.adultMaleStillRight = new TextureRegion(this.adultTextures, 48, 0, 16, 16);
		this.adultFemaleStillLeft = new TextureRegion(this.adultTextures, 0, 240, 16, 16);
		this.adultFemaleStillFace = new TextureRegion(this.adultTextures, 16, 240, 16, 16);
		this.adultFemaleStillBack = new TextureRegion(this.adultTextures, 32, 240, 16, 16);
		this.adultFemaleStillRight = new TextureRegion(this.adultTextures, 48, 240, 16, 16);

		// Timer creation and stamp the startTimer
		this.strTimer = "Time: 0";
		this.fontTimer = new BitmapFont();
		this.timerLayout = new GlyphLayout();

		// Timer creation and stamp the startTimer
		this.strCounter = "Shoppers: 0";
		this.fontCounter = new BitmapFont();
		this.counterLayout = new GlyphLayout();
		
		// Number of agents counter
		this.strCounterEvacuation = "Evacuation: ";
		this.fontCounterEvacuation = new BitmapFont();
		this.counterEvacuationLayout = new GlyphLayout();

		// Display manager
		this.renderWallsHitboxes = false;
		this.renderAgentCircle = false;
		this.renderAheadVector = false;
		this.renderAhead2Vector = false;
		this.renderVelocityVector = false;
		this.renderAvoidanceVector = false;
		this.renderDesiredVelocityVector = false;
		this.renderPaths = false;
		this.renderTarget = false;

		// Loads map
		this.loader = new TmxMapLoader();
		this.map = this.loader.load("map/map2.tmx");
		this.renderer = new OrthogonalTiledMapRenderer(map);
		this.mapWidth = (int) map.getProperties().get("width") * (int) map.getProperties().get("tilewidth");
		this.mapHeight = (int) map.getProperties().get("height") * (int) map.getProperties().get("tileheight");

		// Loads walls
		TiledMapTileLayer wallsLayer = (TiledMapTileLayer) this.renderer.getMap().getLayers().get("Walls");
		for (int x = 0; x < wallsLayer.getWidth(); x++) {
			for (int y = 0; y < wallsLayer.getHeight(); y++) {
				if ((wallsLayer.getCell(x, y)) != null) {
					this.walls.add(new Wall(x * Wall.SIZE, y * Wall.SIZE));
				}
			}
		}

		// Parses the shops and adds them to the shops list
		MapLayer shopsLayer = this.map.getLayers().get("Shops");
		for (MapObject shopObject : shopsLayer.getObjects()) {
			if (shopObject instanceof PolygonMapObject) {
				// Gets the shop ID
				String shopId = shopObject.getName();
				// Gets the polygon area vertices
				float areaVertices[] = ((PolygonMapObject) shopObject).getPolygon().getVertices();
				// Gets shop coordinates
				float x = (float) shopObject.getProperties().get("x");
				float y = (float) shopObject.getProperties().get("y");
				// Creates the shop
				Shop shop = new Shop(shopId, areaVertices, x, y);
				// Adds the shop to the list
				this.shops.add(shop);
			}
		}

		// Parses the shops entrances
		MapLayer shopsEntrancesLayer = this.map.getLayers().get("ShopsEntrances");
		for (MapObject entrancePosition : shopsEntrancesLayer.getObjects()) {
			for (Shop shop : this.shops) {
				if (shop.getId().equals(entrancePosition.getName())) {
					shop.addEntrance((float) entrancePosition.getProperties().get("x"),
							(float) entrancePosition.getProperties().get("y"));
				}
			}
		}

		// Parses the mall exits
		MapLayer exitsLayer = this.map.getLayers().get("Exits");
		for (MapObject exitObject : exitsLayer.getObjects()) {
			float x = (float) exitObject.getProperties().get("x");
			float y = (float) exitObject.getProperties().get("y");
			this.exits.add(new Vector2(x, y));
		}

		// Retrieves the waypoints from the path object layer of the map and build a
		// graph
		MapLayer pathLayer = this.map.getLayers().get("Path");
		for (MapObject waypoint : pathLayer.getObjects()) {
			float xPos = (float) waypoint.getProperties().get("x");
			float yPos = (float) waypoint.getProperties().get("y");
			AStarNode newWaypoint = new AStarNode(xPos, yPos);
			this.waypoints.addNode(newWaypoint);
			for (AStarNode node : this.waypoints.nodes()) {
				if (node != newWaypoint && Vector2.dst(node.x, node.y, newWaypoint.x, newWaypoint.y) < 60f) {
					this.waypoints.putEdge(node, newWaypoint);
				}
			}
		}

		// Sets camera properties
		this.camera = new OrthographicCamera(Gdx.graphics.getWidth() / 5, Gdx.graphics.getHeight() / 5);
		this.camera.position.set(this.mapWidth / 2, this.mapHeight / 2, 0);

		// Initializes font generator and parameter for the timer text
		this.generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/coolvetica.ttf"));
		this.parameter = new FreeTypeFontParameter();
		this.parameter.size = 30;

		// Stage initialization
		VisUI.load();
		this.stage = new Stage(new ScreenViewport());
		this.startButton = new VisTextButton("Start the simulation");
		this.startButton.setSize(200, 100);
		this.startButton.setPosition((Gdx.graphics.getWidth() - this.startButton.getWidth()) / 2,
				(Gdx.graphics.getHeight() - this.startButton.getHeight()) / 2);
		this.startButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				notifyStartingSimulation();
				displayOptionsItem.setDisabled(false);
				spawnerItem.setDisabled(true);
				triggerBombItem.setDisabled(false);
				startButton.remove();
			}
		});
		this.stage.addActor(this.startButton);

		// Menu bar initialization
		Table root = new Table();
		root.setFillParent(true);
		this.stage.addActor(root);
		MenuBar menuBar = new MenuBar();
		root.add(menuBar.getTable()).expandX().fillX().row();
		root.add().expand().fill();

		// Display option window
		VisWindow displayOptionsWindow = new VisWindow("Display options");
		displayOptionsWindow.addCloseButton();
		displayOptionsWindow.fadeIn();
		VisTable optionsTable = new VisTable();
		displayOptionsWindow.add(optionsTable).row();
		// Walls hitboxes
		VisCheckBox renderWallHitboxBox = new VisCheckBox("Render the walls hitboxes");
		renderWallHitboxBox.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				renderWallsHitboxes = !renderWallsHitboxes;
			}
		});
		optionsTable.add(renderWallHitboxBox).left().row();
		// Agents's private circles
		VisCheckBox renderAgentCircleBox = new VisCheckBox("Render the agents private circles");
		renderAgentCircleBox.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				renderAgentCircle = !renderAgentCircle;
			}
		});
		optionsTable.add(renderAgentCircleBox).left().row();
		// Agents's ahead vector
		VisCheckBox renderAheadVectorBox = new VisCheckBox("Render the agents ahead vector");
		renderAheadVectorBox.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				renderAheadVector = !renderAheadVector;
			}
		});
		optionsTable.add(renderAheadVectorBox).left().row();
		// Agents's ahead2 vector
		VisCheckBox renderAhead2VectorBox = new VisCheckBox("Render the agents ahead2 vector");
		renderAhead2VectorBox.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				renderAhead2Vector = !renderAhead2Vector;
			}
		});
		optionsTable.add(renderAhead2VectorBox).left().row();
		// Agents's velocity
		VisCheckBox renderVelocityBox = new VisCheckBox("Render the agents velocity vector");
		renderVelocityBox.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				renderVelocityVector = !renderVelocityVector;
			}
		});
		optionsTable.add(renderVelocityBox).left().row();
		// Agents's avoidance vector
		VisCheckBox renderAvoidanceBox = new VisCheckBox("Render the agents avoidance vector");
		renderAvoidanceBox.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				renderAvoidanceVector = !renderAvoidanceVector;
			}
		});
		optionsTable.add(renderAvoidanceBox).left().row();
		// Agents's desired velocity
		VisCheckBox renderDesiredVelocityBox = new VisCheckBox("Render the agents desired velocity vector");
		renderDesiredVelocityBox.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				renderDesiredVelocityVector = !renderDesiredVelocityVector;
			}
		});
		optionsTable.add(renderDesiredVelocityBox).left().row();
		// Agents's path
		VisCheckBox renderPathBox = new VisCheckBox("Render the agents paths");
		renderPathBox.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				renderPaths = !renderPaths;
			}
		});
		optionsTable.add(renderPathBox).left().row();
		// Agents's target
		VisCheckBox renderTargetBox = new VisCheckBox("Render the agents targets");
		renderTargetBox.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				renderTarget = !renderTarget;
			}
		});
		optionsTable.add(renderTargetBox).left().row();
		displayOptionsWindow.pack();

		// Display option window
		VisWindow spawnerParametersWindow = new VisWindow("Spawner parameters");
		spawnerParametersWindow.addCloseButton();
		spawnerParametersWindow.fadeIn();
		VisTable parametersTable = new VisTable();
		spawnerParametersWindow.add(parametersTable).row();
		// Number of agents to spawn
		VisLabel nbAgentLabel = new VisLabel("Number of agents to spawn per spawner : ");
		parametersTable.add(nbAgentLabel).left().row();
		VisValidatableTextField nbAgentTextField = new VisValidatableTextField();
		nbAgentTextField.setText("150");
		parametersTable.add(nbAgentTextField).left().row();
		// Spawn frequency
		VisLabel frequencyLabel = new VisLabel("Spawn frequency (in ms) : ");
		parametersTable.add(frequencyLabel).left().row();
		VisValidatableTextField frequencyTextField = new VisValidatableTextField();
		frequencyTextField.setText("1000");
		parametersTable.add(frequencyTextField).left().row();
		VisLabel errorLabel = new VisLabel();
		errorLabel.setColor(Color.RED);
		parametersTable.add(errorLabel).left().row();
		VisTextButton acceptButton = new VisTextButton("Accept");
		parametersTable.add(acceptButton).expand().fill().row();
		SimpleFormValidator validator = new SimpleFormValidator(acceptButton, errorLabel, "smooth");
		validator.setSuccessMessage("OK!");
		validator.integerNumber(nbAgentTextField, "Incorrect number of agents.");
		validator.valueGreaterThan(nbAgentTextField, "Incorrect number of agents.", 0, false);
		validator.integerNumber(frequencyTextField, "Incorrect frequency.");
		validator.valueGreaterThan(frequencyTextField, "Frequency should be at least 150ms.", 150, true);
		acceptButton.addListener(new ChangeListener() {
		  @Override 
		  public void changed(ChangeEvent event, Actor actor) {
				nbAgents = Integer.parseInt(nbAgentTextField.getText());
				spawnerFrequency = Integer.parseInt(frequencyTextField.getText());
				spawnerParametersWindow.remove();
		  }});
		spawnerParametersWindow.pack();

		
		// Options menu
		Menu optionsMenu = new Menu("Options");
		menuBar.addMenu(optionsMenu);
		// "Spawner options"
		spawnerItem = new MenuItem("Spawner parameters", new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				stage.addActor(spawnerParametersWindow);
			}
		});
		optionsMenu.addItem(spawnerItem);
		// "Display options"
		displayOptionsItem = new MenuItem("Display options", new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				stage.addActor(displayOptionsWindow);
			}
		});
		displayOptionsItem.setDisabled(true);
		optionsMenu.addItem(displayOptionsItem);
		// "Trigger a bomb"
		triggerBombItem = new MenuItem("Trigger bomb");
		triggerBombItem.setDisabled(true);
		triggerBombItem.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				triggerBombItem.setDisabled(true);
				bombTriggered = true;
				// Triggers an alarm sound
				Sound alarm = Gdx.audio.newSound(Gdx.files.internal("sounds/alarm.wav"));
				long id = alarm.play(1.0f);
				alarm.setLooping(id, true);
				evacuationStartTime = System.currentTimeMillis();
			}
		});
		optionsMenu.addItem(triggerBombItem);

		// Help menu
		Menu helpMenu = new Menu("Help");
		menuBar.addMenu(helpMenu);
		// About window
		VisWindow aboutWindow = new VisWindow("About the authors...");
		aboutWindow.addCloseButton();
		aboutWindow.fadeIn();
		VisTable aboutTable = new VisTable();
		aboutWindow.add(aboutTable).row();
		VisLabel namesLabel = new VisLabel("AING André & LEGENDRE Emile");
		aboutTable.add(namesLabel).left().row();
		VisLabel githubLinkLabel = new LinkLabel("Go to the GitHub repository.", "https://github.com/Piryus/MallEscape-AI");
		aboutTable.add(githubLinkLabel).left().row();
		// "About"
		MenuItem aboutItem = new MenuItem("About...");
		aboutItem.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				stage.addActor(aboutWindow);
			}
		});
		helpMenu.addItem(aboutItem);
		aboutWindow.pack();


		// Adds input processors
		this.inputMultiplexer = new InputMultiplexer();
		this.inputMultiplexer.addProcessor(this.stage);
		this.inputMultiplexer.addProcessor(this);
		Gdx.input.setInputProcessor(this.inputMultiplexer);
	}

	@Override
	public void dispose() {
		this.spriteBatch.dispose();
		this.fixedSpriteBatch.dispose();
		this.generator.dispose();
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub
	}

	@Override
	public void render() {
		// Sets background color
		Gdx.gl.glClearColor((float) 97 / 255, (float) 133 / 255, (float) 248 / 255, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		this.camera.update();

		// Renders map
		this.renderer.setView(this.camera);
		this.renderer.render();

		// Renders the bodies on the map
		this.spriteBatch.setProjectionMatrix(this.camera.combined);
		this.spriteBatch.begin();
		for (AgentBody body : this.bodies) {
			if (body instanceof AdultBody) {
				if (((AdultBody) body).getSex() == Sex.male) {
					if (body.getLinearVelocity().angle() > 315f || body.getLinearVelocity().angle() <= 45f) {
						this.spriteBatch.draw(this.adultMaleStillRight, body.getPosition().x - 8, body.getPosition().y);
					} else if (body.getLinearVelocity().angle() > 45f && body.getLinearVelocity().angle() <= 135f) {
						this.spriteBatch.draw(this.adultMaleStillBack, body.getPosition().x - 8, body.getPosition().y);
					} else if (body.getLinearVelocity().angle() > 135f && body.getLinearVelocity().angle() <= 225f) {
						this.spriteBatch.draw(this.adultMaleStillLeft, body.getPosition().x - 8, body.getPosition().y);
					} else if (body.getLinearVelocity().angle() > 225f && body.getLinearVelocity().angle() <= 315f) {
						this.spriteBatch.draw(this.adultMaleStillFace, body.getPosition().x - 8, body.getPosition().y);
					}
				} else {
					if (body.getLinearVelocity().angle() > 315f || body.getLinearVelocity().angle() <= 45f) {
						this.spriteBatch.draw(this.adultFemaleStillRight, body.getPosition().x - 8, body.getPosition().y);
					} else if (body.getLinearVelocity().angle() > 45f && body.getLinearVelocity().angle() <= 135f) {
						this.spriteBatch.draw(this.adultFemaleStillBack, body.getPosition().x - 8, body.getPosition().y);
					} else if (body.getLinearVelocity().angle() > 135f && body.getLinearVelocity().angle() <= 225f) {
						this.spriteBatch.draw(this.adultFemaleStillLeft, body.getPosition().x - 8, body.getPosition().y);
					} else if (body.getLinearVelocity().angle() > 225f && body.getLinearVelocity().angle() <= 315f) {
						this.spriteBatch.draw(this.adultFemaleStillFace, body.getPosition().x - 8, body.getPosition().y);
					}
				}

			}
		}
		this.spriteBatch.end();

		// Renders the forces applied on the agents
		this.shapeRenderer.setProjectionMatrix(this.camera.combined);
		this.shapeRenderer.begin(ShapeType.Line);
		if (this.renderWallsHitboxes) {
			for (SimulationEntity wall : this.walls) {
				if (wall instanceof SimulationEntity) {
					this.shapeRenderer.setColor(1, 1, 1, 1); // White
					// Global
					this.shapeRenderer.circle(wall.getPosition().x + 8, wall.getPosition().y + 8, 12);
					// Bottom
					this.shapeRenderer.circle(wall.getPosition().x + 4, wall.getPosition().y + 5, 6);
					this.shapeRenderer.circle(wall.getPosition().x + 8, wall.getPosition().y + 5, 6);
					this.shapeRenderer.circle(wall.getPosition().x + 12, wall.getPosition().y + 5, 6);
					// Middle
					this.shapeRenderer.circle(wall.getPosition().x + 4, wall.getPosition().y + 8, 6);
					this.shapeRenderer.circle(wall.getPosition().x + 8, wall.getPosition().y + 8, 6);
					this.shapeRenderer.circle(wall.getPosition().x + 12, wall.getPosition().y + 8, 6);
					// Top
					this.shapeRenderer.circle(wall.getPosition().x + 4, wall.getPosition().y + 11, 6);
					this.shapeRenderer.circle(wall.getPosition().x + 8, wall.getPosition().y + 11, 6);
					this.shapeRenderer.circle(wall.getPosition().x + 12, wall.getPosition().y + 11, 6);

				}
			}
		}

		for (AgentBody body : this.bodies) {
			if (body instanceof AdultBody) {
				// Agent's private circle
				if (renderAgentCircle) {
					this.shapeRenderer.setColor(0, 0, 1, 1); // Blue
					this.shapeRenderer.circle(body.getPosition().x, body.getPosition().y, 10);
				}
				// Agent's ahead vector
				if (renderAheadVector) {
					this.shapeRenderer.setColor(1, 1, 1, 1); // White
					this.shapeRenderer.line(body.getPosition().x, body.getPosition().y, body.getAhead().x,
							body.getAhead().y);
				}
				// Agent's ahead2 vector
				if (renderAhead2Vector) {
					this.shapeRenderer.setColor(1, 0, 0, 1); // Red
					this.shapeRenderer.line(body.getPosition().x, body.getPosition().y, body.getAhead2().x,
							body.getAhead2().y);
				}
				// Agent's velocity vector
				if (renderVelocityVector) {
					this.shapeRenderer.setColor(0, 1, 0, 1); // Green
					this.shapeRenderer.line(body.getPosition().x, body.getPosition().y,
							body.getPosition().x + body.getLinearVelocity().x,
							body.getPosition().y + body.getLinearVelocity().y);
				}
				// Agent's avoidance vector
				if (renderAvoidanceVector) {
					this.shapeRenderer.setColor(0.5f, 0, 0.5f, 1); // Purple
					this.shapeRenderer.line(body.getPosition().x, body.getPosition().y,
							body.getPosition().x + body.getAvoidance().x, body.getPosition().y + body.getAvoidance().y);
				}

				// Agent's desired velocity vector
				if (renderDesiredVelocityVector) {
					this.shapeRenderer.setColor(1, 0, 0, 1); // Red
					this.shapeRenderer.line(body.getPosition().x, body.getPosition().y,
							body.getPosition().x + body.getDesiredVelocity().x,
							body.getPosition().y + body.getDesiredVelocity().y);
				}

				// Renders agents' paths
				if (renderPaths && body.getPath() != null && !body.getPath().getNodes().isEmpty()) {
					this.shapeRenderer.setColor(1, 0, 0, 1);
					for (Vector2 node : body.getPath().getNodes()) {
						this.shapeRenderer.circle(node.x, node.y, 5);
					}
					for (int i = 0; i < body.getPath().getNodes().size() - 1; i++) {
						this.shapeRenderer.line(body.getPath().getNodes().get(i), body.getPath().getNodes().get(i + 1));
					}
				}

				// Renders agents' target
				if (renderTarget) {
					this.shapeRenderer.setColor(1, 0, 0, 1);
					this.shapeRenderer.circle(body.getTarget().x, body.getTarget().y, 5);
				}
			}
		}
		this.shapeRenderer.end();

		// Renders the red overlay when the bomb has been planted
		if (bombTriggered) {
			Gdx.graphics.getGL20().glEnable(GL20.GL_BLEND);
			Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
			this.shapeRenderer.begin(ShapeType.Filled);
			this.shapeRenderer.setProjectionMatrix(
					new Matrix4().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));
			this.shapeRenderer.setColor(
					new Color(1, 0, 0, (float) Math.cos(Math.PI / 2 * System.currentTimeMillis() / 1000) * 0.5f));
			this.shapeRenderer.rect(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
			this.shapeRenderer.end();
			Gdx.gl.glDisable(GL20.GL_BLEND);
		}

		// Timer rendering
		// Changes the parameter text to the current time
		this.parameter.characters = this.strTimer;
		// Generates the timer bitmap
		this.fontTimer = this.generator.generateFont(this.parameter);
		// Sets layout (to get width then)
		this.timerLayout.setText(this.fontTimer, this.strTimer);
		// Renders timer
		this.fixedSpriteBatch.begin();
		this.fixedSpriteBatch.setProjectionMatrix(
				new Matrix4().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));
		this.fontTimer.setColor(1.0f, 1.0f, 1.0f, 1.0f);
		float posX = (Gdx.graphics.getWidth() - this.timerLayout.width) / 2;
		float posY = this.timerLayout.height + 10;
		this.fontTimer.draw(this.fixedSpriteBatch, this.strTimer, posX, posY);
		this.fixedSpriteBatch.end();

		// Counter rendering
		// Changes the parameter text to the current time
		this.parameter.characters = this.strCounter;
		// Generates the timer bitmap
		this.fontCounter = this.generator.generateFont(this.parameter);
		// Sets layout (to get width then)
		this.counterLayout.setText(this.fontCounter, this.strCounter);
		// Renders counter
		this.fixedSpriteBatch.begin();
		this.fontCounter.setColor(1.0f, 1.0f, 1.0f, 1.0f);
		posX = (Gdx.graphics.getWidth() - this.counterLayout.width) / 2;
		posY = Gdx.graphics.getHeight() - 10 - this.counterLayout.height;
		this.fontCounter.draw(this.fixedSpriteBatch, this.strCounter, posX, posY);
		this.fixedSpriteBatch.end();
		
		// Evacuation counter rendering
		if (bombTriggered) {
			// Changes the parameter text to the current time
			this.parameter.characters = this.strCounterEvacuation;
			// Generates the timer bitmap
			this.fontCounterEvacuation = this.generator.generateFont(this.parameter);
			// Sets layout (to get width then)
			this.counterEvacuationLayout.setText(this.fontCounterEvacuation, this.strCounterEvacuation);
			// Renders counter
			this.fixedSpriteBatch.begin();
			this.fontCounterEvacuation.setColor(1.0f, 1.0f, 1.0f, 1.0f);
			posX = (Gdx.graphics.getWidth() - this.counterEvacuationLayout.width) / 2;
			posY = this.timerLayout.height + counterEvacuationLayout.height + 20;
			this.fontCounterEvacuation.draw(this.fixedSpriteBatch, this.strCounterEvacuation, posX, posY);
			this.fixedSpriteBatch.end();
		}

		// Stage rendering
		float delta = Gdx.graphics.getDeltaTime();
		this.stage.act(delta);
		this.stage.draw();

	}

	@Override
	public void resize(int width, int height) {
		this.stage.getViewport().update(width, height, true);
		this.startButton.setPosition((Gdx.graphics.getWidth() - this.startButton.getWidth()) / 2,
				(Gdx.graphics.getHeight() - this.startButton.getHeight()) / 2);
	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean keyDown(int arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean keyTyped(char arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean keyUp(int arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean mouseMoved(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		// Manages camera zoom
		if (amount == 1 && this.camera.zoom >= 1) {
			this.camera.zoom++;
		} else if (amount == -1 && this.camera.zoom > 1) {
			this.camera.zoom--;
		}
		return false;
	}

	@Override
	public boolean touchDown(int x, int y, int pointer, int button) {
		this.lastTouch.set(x, y);
		return false;
	}

	@Override
	public boolean touchDragged(int x, int y, int pointer) {
		float deltaX = Gdx.input.getDeltaX();
		float deltaY = Gdx.input.getDeltaY();
		this.camera.translate(-deltaX, deltaY);
		this.camera.update();
		return false;
	}

	@Override
	public boolean touchUp(int x, int y, int pointer, int button) {
		return false;
	}

	@Override
	public void update(List<AgentBody> bodies, float time) {
		this.bodies = bodies;
		this.strTimer = "Time: " + String.format("%.2f", time) + "s";
		this.strCounter = "Shoppers: " + bodies.size();
		if (bombTriggered) {
			if (bodies.size() > 0) {
				this.elapsedTime = (System.currentTimeMillis() - evacuationStartTime) / 1000f;
			}
			this.strCounterEvacuation = "Evacuation: " + String.format("%.2f", elapsedTime) + "s";	
		}
	}

	public List<Wall> getWalls() {
		return this.walls;
	}

	public MutableGraph<AStarNode> getWaypoints() {
		return this.waypoints;
	}

	/**
	 * Wakes up the environment agent to start the simulation
	 */
	public void notifyStartingSimulation() {
		synchronized (this) {
			this.notify();
		}
	}

	public List<Shop> getShops() {
		return this.shops;
	}

	public boolean isBombTriggered() {
		return this.bombTriggered;
	}

	public List<Vector2> getExits() {
		return this.exits;
	}

	/**
	 * @return the spawnerFrequency
	 */
	public int getSpawnerFrequency() {
		return spawnerFrequency;
	}

	/**
	 * @return the nbAgents
	 */
	public int getNbAgents() {
		return nbAgents;
	}
}

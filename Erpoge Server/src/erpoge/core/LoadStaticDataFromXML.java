package erpoge.core;
import static org.w3c.dom.Node.ELEMENT_NODE;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.jgrapht.DirectedGraph;
import org.jgrapht.EdgeFactory;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class LoadStaticDataFromXML {
	private static final EdgeFactory<BodyPartTypeInstance, DefaultEdge> defaultEdgeFactory = new EdgeFactory<BodyPartTypeInstance, DefaultEdge>() {
		public DefaultEdge createEdge(BodyPartTypeInstance v1, BodyPartTypeInstance v2) {
			return new DefaultEdge();
		}
	};
	/**
	 * Reads .xml file with game objects' text descriptions and loads
	 * the descriptions into memory as data structures to be used in
	 * in-game calculations and client-side static data building.
	 * 
	 * @param filename
	 * 			Path to .xml data file.
	 */
	public static void loadGameDataFromXml(String filename) {
		try {
			File file = new File(filename);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(file);
			Element eRoot = doc.getDocumentElement();
			String textOfFile = new Scanner(file, "UTF-8").useDelimiter("\\A").next();
			validate(textOfFile, "data/schema.xsd");
			removeWhitespaceTextNodes(eRoot);

			// Remove all blank text nodes between elements
			// (these nodes are there because XML is formatted in
			// a human-readable way).
			loadCharacters(eRoot);
			loadMaterials(eRoot);
			loadSounds(eRoot);
			loadItems(eRoot);
			loadObjects(eRoot);
			loadFloors(eRoot);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private static void removeWhitespaceTextNodes(Node node) {
		NodeList nlChildren = node.getChildNodes();
		for (int i=nlChildren.getLength()-1; i>-1; i--) {
			Node nChild = nlChildren.item(i);
			if (nChild.getNodeType() == Node.ELEMENT_NODE) {
				removeWhitespaceTextNodes(nChild);
			}
			if (nChild.getNodeType() == Node.TEXT_NODE && nChild.getNodeValue().trim().length() == 0) {
				nChild.getParentNode().removeChild(nChild);
			}
		}
	}
	public static boolean validate(String inputXml, String schemaLocation) throws SAXException, IOException {
		SchemaFactory factory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
		File schemaFile = new File(schemaLocation);
		Schema schema = factory.newSchema(schemaFile);
		Validator validator = schema.newValidator();

		Source source = new StreamSource(new StringReader(inputXml));

		boolean isValid = true;
		try {
			validator.validate(source);
		} catch (SAXException e) {
			e.printStackTrace();
			isValid = false;
		}

		return isValid;
	}
	private static void loadSounds(Element eRoot) {
		Element eSounds = (Element) eRoot.getElementsByTagName("sounds").item(0);
		for (
			Element eSound = (Element) eSounds.getFirstChild();
			eSound != null;
			eSound = (Element) eSound.getNextSibling()
		) {
			String name = eSound.getElementsByTagName("name").item(0).getFirstChild().getNodeValue();
			int bass = Integer.parseInt(eSound.getElementsByTagName("bass").item(0).getFirstChild().getNodeValue());
			int mid = Integer.parseInt(eSound.getElementsByTagName("mid").item(0).getFirstChild().getNodeValue());
			int treble = Integer.parseInt(eSound.getElementsByTagName("treble").item(0).getFirstChild().getNodeValue());
			SoundType soundType = new SoundType(name, bass, mid, treble);
			StaticData.add(soundType);
		}
	}
	private static void loadCharacters(Element eRoot) {
		Element eCharacters = (Element) eRoot.getElementsByTagName("characters").item(0);
		for (
			Element eCharacter = (Element) eCharacters.getFirstChild();
			eCharacter != null;
			eCharacter = (Element) eCharacter.getNextSibling()
		) {
		// Form a CharacterType object
			String name = eCharacter.getElementsByTagName("name").item(0).getFirstChild().getNodeValue();
			double weight = Double.parseDouble(eCharacter.getElementsByTagName("weight").item(0).getFirstChild().getNodeValue());
			double height = Double.parseDouble(eCharacter.getElementsByTagName("height").item(0).getFirstChild().getNodeValue());
			HashSet<CharacterAspect> aspects = new HashSet<CharacterAspect>();
			for (
				Element eAspect = (Element) eCharacter.getElementsByTagName("aspects").item(0).getFirstChild();
				eAspect != null;
				eAspect = (Element) eAspect.getNextSibling()
			) {
			// For each aspect inside XML element <aspects/> add a CharacterAspect to CharacterType
				aspects.add(CharacterAspect.getByName(eAspect.getTagName()));
			}
			DirectedGraph<BodyPartTypeInstance, DefaultEdge> bodyGraph = xml2BodyGraph((Element)eCharacter.getElementsByTagName("body").item(0).getFirstChild());
			CharacterType characterType = new CharacterType(name, aspects, weight, height, bodyGraph);
			StaticData.add(characterType);
		}
	}
	private static void loadMaterials(Element eRoot) {
		Element eMaterials = (Element) eRoot.getElementsByTagName("materials").item(0);
		for (
			Element eMaterial = (Element) eMaterials.getFirstChild();
			eMaterial != null;
			eMaterial = (Element) eMaterial.getNextSibling()
		) {
			int durability = Integer.parseInt(eMaterial.getElementsByTagName("durability").item(0).getFirstChild().getNodeValue());
			String name = eMaterial.getElementsByTagName("name").item(0).getFirstChild().getNodeValue();
			int density = Integer.parseInt(eMaterial.getElementsByTagName("density").item(0).getFirstChild().getNodeValue());
			Material material = new Material(name, durability, density);
			StaticData.add(material);
		}
	}
	private static void loadObjects(Element eRoot) {
		Element eObjects = (Element) eRoot.getElementsByTagName("objects").item(0);
		
		for (
			Element eObject = (Element) eObjects.getFirstChild();
			eObject != null;
			eObject = (Element) eObject.getNextSibling()
		) {
			String name = eObject.getElementsByTagName("name").item(0).getFirstChild().getNodeValue();
			Element ePassability = (Element) eObject.getElementsByTagName("passability").item(0);
			int passability = StaticData.PASSABILITY_NONE;
			boolean isUsable;
			for (
				Element ePassType = (Element) ePassability.getFirstChild();
				ePassType != null;
				ePassType = (Element) ePassType.getNextSibling()
			) {
			/* 
			 * <passability /> element can contain several flag elements, that 
			 * determine what types of passability this ObjectType has: visual,
			 * walkable.
			 */
				if (ePassType.getTagName().equals("none")) {
					passability = StaticData.PASSABILITY_NONE;
					break;
				}
				if (ePassType.getTagName().equals("all")) {
					passability = StaticData.PASSABILITY_VISUAL+StaticData.PASSABILITY_WALKABLE;
					break;
				}
				if (ePassType.getTagName().equals("visual")) {
					passability += StaticData.PASSABILITY_VISUAL;
				} else if (ePassType.getTagName().equals("walkable")) {
					passability += StaticData.PASSABILITY_WALKABLE;
				}
			}
			if (eObject.getElementsByTagName("usable").getLength() == 1) {
				isUsable = true;
			} else {
				isUsable = false;
			}
			int objectClass = ObjectType.CLASS_DEFAULT;
			if (eObject.getElementsByTagName("type").getLength() == 1) {
				String objectClassAsString = eObject.getElementsByTagName("type").item(0).getFirstChild().getNodeValue();
				if (objectClassAsString.equals("wall")) {
					objectClass = ObjectType.CLASS_WALL;
				} else if (objectClassAsString.equals("door")) {
					objectClass = ObjectType.CLASS_DOOR;
				} else if (objectClassAsString.equals("interlevel")) {
					objectClass = ObjectType.CLASS_INTERLEVEL;
				} else {
					throw new RuntimeException("Unknown object class "+objectClassAsString);
				}
			}
			ObjectType objectType = new ObjectType(name, passability, isUsable, objectClass);
			StaticData.add(objectType);
		}
	}
	private static void loadFloors(Element eRoot) {
		Element eFloors = (Element) eRoot.getElementsByTagName("floors").item(0);
		for (
			Element eFloor = (Element) eFloors.getFirstChild();
			eFloor != null;
			eFloor = (Element) eFloor.getNextSibling()
		) {
			String name = eFloor.getElementsByTagName("name").item(0).getFirstChild().getNodeValue();
			FloorType floorType = new FloorType(name);
			StaticData.add(floorType);
		}
	}
	private static Element getChild(Element parent, String name) {
		for (Node child = parent.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child instanceof Element && name.equals(child.getNodeName())) {
				return (Element) child;
			}
		}
		return null;
	}	
	private static void loadItems(Element eRoot) {
		for (
			Element eItem = (Element) eRoot.getElementsByTagName("items").item(0).getFirstChild();
			eItem != null;
			eItem = (Element) eItem.getNextSibling()
		) {
			// Parsing item type's properties
			String name = eItem.getElementsByTagName("name").item(0).getFirstChild().getNodeValue();
			String materialName = getChild(eItem, "material").getFirstChild().getNodeValue();
			Material material = StaticData.getMaterialByName(materialName);
			HashSet<Aspect> aspects = new HashSet<Aspect>();
			double weight = Double.parseDouble(eItem.getElementsByTagName("weight").item(0).getFirstChild().getNodeValue());
			double volume = Double.parseDouble(eItem.getElementsByTagName("volume").item(0).getFirstChild().getNodeValue());
			boolean stackable;
			if (eItem.getElementsByTagName("stackable").getLength() == 1) {
				stackable = true;
			} else {
				stackable = false;
			}
			if (eItem.getElementsByTagName("stackable").getLength() == 1) {
				stackable = true;
			}
			// Parsing item type's aspects
			Node nAspects = eItem.getElementsByTagName("aspects").item(0);
			if (nAspects != null) {
				for (
					Element eAspect = (Element) nAspects.getFirstChild();
					eAspect != null; 
					eAspect = (Element) eAspect.getNextSibling()
				) {
					if (eAspect.getNodeType() != ELEMENT_NODE) {
						continue;
					}
					if (eAspect.getTagName().equals("rangedWeapon")) {
						String sAmmoType = eAspect.getElementsByTagName("ammo").item(0).getFirstChild().getNodeValue();
						int iReloadTime = Integer.parseInt(eAspect.getElementsByTagName("reloadTime").item(0).getFirstChild().getNodeValue());
						int iAimTime = Integer.parseInt(eAspect.getElementsByTagName("aimTime").item(0).getFirstChild().getNodeValue());
						int iMagazine = Integer.parseInt(eAspect.getElementsByTagName("magazine").item(0).getFirstChild().getNodeValue());
						aspects.add(new AspectRangedWeapon(iReloadTime, iAimTime, iMagazine, sAmmoType));
					}
					if (eAspect.getTagName().equals("craftable")) {
						
					}
					if (eAspect.getTagName().equals("apparel")) {
						Graph<BodyPartTypeInstance, DefaultEdge> form = xml2BodyGraph((Element)eAspect.getElementsByTagName("form").item(0).getFirstChild());
						HashSet<BodyPartType> covers = new HashSet<BodyPartType>();
						HashSet<BodyPartType> blocks = new HashSet<BodyPartType>();
						if (eAspect.getElementsByTagName("covers").getLength() > 0) {
							for (
								Element eCovers = (Element) eAspect.getElementsByTagName("covers").item(0).getFirstChild();
								eCovers != null;
								eCovers = (Element) eCovers.getNextSibling()
							) {
								covers.add(BodyPartType.string2BodyPart(eCovers.getTagName()));
							}
						}
						if (eAspect.getElementsByTagName("blocks").getLength() > 0) {
							for (
								Element eBlocks = (Element) eAspect.getElementsByTagName("blocks").item(0).getFirstChild();
								eBlocks != null;
								eBlocks = (Element) eBlocks.getNextSibling()
							) {
								blocks.add(BodyPartType.string2BodyPart(eBlocks.getTagName()));
							}
						}
						aspects.add(new AspectApparel(form, covers, blocks));
					}
					if (eAspect.getTagName().equals("container")) {
						double containerVolume = Double.parseDouble(eAspect.getElementsByTagName("volume").item(0).getFirstChild().getNodeValue());
						boolean liquidAllowing;
						if (eAspect.getElementsByTagName("liquidAllowing").getLength() == 1) {
							liquidAllowing = true;
						} else {
							liquidAllowing = false;
						}
						aspects.add(new AspectContainer(containerVolume, liquidAllowing));
					}
				}
			}
			ItemType itemType = new ItemType(
				name, 
				aspects, 
				weight, 
				volume,
				material,
				stackable
			);
			StaticData.add(itemType);
		}
	}
	/**
	 * Creates a graph of body organs from xml. 
	 * @param eRoot The root element of a body organ structure (not <form/> element, but the root body organ).
	 * @return
	 */
	private static DirectedGraph<BodyPartTypeInstance, DefaultEdge> xml2BodyGraph(Element eRoot) {
		DefaultDirectedGraph<BodyPartTypeInstance, DefaultEdge> graph = new DefaultDirectedGraph<BodyPartTypeInstance, DefaultEdge>(defaultEdgeFactory);
		BodyPartTypeInstance rootVertex = new BodyPartTypeInstance(BodyPartType.string2BodyPart(eRoot.getTagName()));
		graph.addVertex(rootVertex);
		xml2Graph(eRoot, graph, rootVertex);
		return graph;
	}
	/**
	 * A recursive helper method for {@link LoadStaticDataFromXML#xml2BodyGraph(Element)}.
	 * 
	 * @param eRoot
	 * @param graph
	 * @param rootVertex
	 */
	private static void xml2Graph(Element eRoot, DefaultDirectedGraph<BodyPartTypeInstance, DefaultEdge> graph, BodyPartTypeInstance rootVertex) {
		for (
			Element eChild = (Element) eRoot.getFirstChild();
			eChild != null; 
			eChild = (Element) eChild.getNextSibling()
		) {
			BodyPartTypeInstance newVertex = new BodyPartTypeInstance(BodyPartType.string2BodyPart(eChild.getTagName()));
			graph.addVertex(newVertex);
			try {
				graph.addEdge(rootVertex, newVertex);
			} catch (IllegalArgumentException e) {
				System.out.println("error adding "+newVertex.type+" to "+rootVertex.type);
				System.out.println(graph.vertexSet());
				e.printStackTrace();
			}
			xml2Graph(eChild, graph, newVertex);
		}
	}
}

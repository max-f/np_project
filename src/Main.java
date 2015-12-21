package npproject.improved.parallel;

import java.io.File;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CyclicBarrier;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Main class contains the main method. 
 * Includes the sequential functionality of the program.
 * 
 */
public class Main {
	private static final int threadCount = Runtime.getRuntime().availableProcessors() + 1;
	private static final Thread[] threads = new Thread[threadCount];
	private static final CyclicBarrier barrier = new CyclicBarrier(threadCount);
	private static ConcurrentMap<Integer, Mesh> partition = new ConcurrentHashMap<Integer, Mesh>(); 
	private static ConcurrentMap<Integer, Vertex> vertices = new ConcurrentHashMap<Integer, Vertex>();
	
	/**
	 * Main thread parses the input file and builds the initial partition before it
	 * creates the splitting threads. Those are given, among others, references to a cyclic barrier 
	 * and the list of meshes representing the partition.
	 * Waits for the splitting threads to end and exits with the result.
	 * 
	 * @param args
	 *  		Path to the xml input file(s)
	 */
	public static void main(String[] args) {
		// Check whether xml file is given
		if(args.length != 1) {
			System.out.println("One XML file expected");
			System.exit(1);
		}
		// parse and build partition
		transformPartition(parse(args[0]));
		// Create splitting threads
		for(int i = 0; i < threadCount; i++) {
			threads[i] = new Thread(new Splitter(partition, vertices, barrier, threadCount));
			threads[i].start();
		}
		// Wait for all threads to end
		for(Thread t : threads) {
			try {
				t.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.exit(partition.size());
	}
	
	/**
	 * Parses the xml input file, creating all edge and vertex objects.
	 * Incoming edges are stored in each vertex object, vertices are stored in the map vertices.
	 * 
	 * @param filepath
	 *  		Path to the xml input file
	 */
	public static Map<Integer, Mesh> parse(String filepath) {
		Map<Integer, Mesh> initialPartition = new HashMap<Integer, Mesh>();
		try {
			// Parse file and create document object
			File xmlfile = new File(filepath);
			DocumentBuilderFactory dbfactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbfactory.newDocumentBuilder();
			Document doc = db.parse(xmlfile);
			// Get node and edge elements
			NodeList ver = doc.getElementsByTagName("node");
			NodeList ed = doc.getElementsByTagName("edge");
			// Read vertices
			for(int i = 0; i < ver.getLength(); i++) {
				Element node = (Element) ver.item(i);
				int id = Integer.parseInt(node.getElementsByTagName("id").item(0).getTextContent().trim());
				int capacity = Integer.parseInt(node.getElementsByTagName("capacity").item(0).getTextContent().trim());
				Vertex v = new Vertex(id, capacity);
				vertices.put(id, v);
				// build initial partition via capacity
				Mesh a = initialPartition.get(capacity);
				if (a == null){
					Mesh m = new Mesh(capacity);
					m.addVertex(v);
					initialPartition.put(capacity, m);
					
				} else  {
					a.addVertex(v);
				}
			}
			// Read edges
			for(int i = 0; i < ed.getLength(); i++) {
				Element edge = (Element) ed.item(i);
				int srcId = Integer.parseInt(edge.getElementsByTagName("srcid").item(0).getTextContent().trim());
				int dstId = Integer.parseInt(edge.getElementsByTagName("dstid").item(0).getTextContent().trim());
				Edge e = new Edge();
				e.setSrcId(srcId);
				e.setDestId(dstId);
				vertices.get(dstId).addIE(e);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return initialPartition;
	}
	
	/**
	 * Transforms the given initialPartition Map to partition.
	 * Mapping switched from capacity -> mesh to ID -> mesh.
	 * Furthermore the leadingToEdges in every mesh is updated.
	 * 
	 * @param initialPartition
	 *  		The given initalPartition Map
	 */
	public static void transformPartition(Map<Integer, Mesh> initialPartition) {
		for(Mesh m : initialPartition.values()) {
			m.setMeshId(partition.size());
			m.updateLE();
			partition.put(partition.size(), m);
		}
	}

}

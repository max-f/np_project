package npproject.improved.parallel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Class represents one mesh in the partition of the graph.
 * 
 */
public class Mesh {
	private int capacity;
	private int meshId;
	private Map<Integer, Vertex> vertices;
	private List<Edge> leadingToEdges;
	private List<Vertex> toDo;
	private boolean wasSplitter;
	private boolean toBeSplit;
	
	/**
	 * Constructs a mesh.
	 */
	public Mesh(int capacity) {
		this.capacity = capacity;
		this.vertices = new HashMap<Integer, Vertex>();
		this.leadingToEdges = new ArrayList<Edge>();
		this.toDo = new ArrayList<Vertex>();
		this.wasSplitter = false;
		this.toBeSplit = false;
	}
	
	/**
	 * Adds list of vertices of one inverse image to the todo list.
	 * 
	 * @param vertices
	 *  		List of vertices to add.
	 */
	public void addToDo(List<Vertex> vertices) {
		toDo.addAll(vertices);
	}
	
	/**
	 * Removes all vertices from todo list.
	 */
	public void clearToDo() {
		toDo.clear();
	}
	
	/**
	 * Returns list of vertices on whose meshes splitting has to be still calculated.
	 * 
	 * @return list of vertices to be processed.
	 */
	public List<Vertex> getToDo() {
		return toDo;
	}
	
	/**
	 * Checks wether this mesh was already used/being used as splitter.
	 * Synchronized to guarantee thread safety, set to true in same call if not used yet.
	 * 
	 * @return false if not already used, true otherwise.
	 */
	public synchronized boolean getWasSplitter() {
		if(wasSplitter)
			return true;
		else{
			wasSplitter = true;
			return false;
		}
	}
	
	/**
	 * Set whether this mesh was already used as splitter.
	 * 
	 * @param b
	 *  		Value wasSplitter to be set to.
	 */
	public synchronized void setWasSplitter(boolean b) {
		this.wasSplitter = b;
	}
	
	/**
	 * Returns the capacity of the mesh.
	 * 
	 * @return the capacity of the mesh.
	 */
	public int getCapacity() {
		return capacity;
	}
	
	/**
	 * Set the ID of the mesh.
	 * 
	 * @param meshId
	 *  		Value the meshId to be set to.
	 */
	public void setMeshId(int meshId) {
		this.meshId = meshId;
	}
	
	/**
	 * Returns the ID of the mesh.
	 * 
	 * @return the meshId.
	 */
	public int getMeshId() {
		return meshId;
	}
	
	/**
	 * Returns all vertices currently in the mesh.
	 * 
	 * @return vertices in the mesh.
	 */
	public Map<Integer, Vertex> getVertices() {
		return vertices;
	}
	
	/**
	 * Returns the list of edges leading into/within the mesh.
	 * 
	 * @return the leadingToEdges list.
	 */
	public List<Edge> getLE() {
		return leadingToEdges;
	}
	
	/**
	 * Adds vertex to the mesh.
	 * 
	 * @param v
	 *  		Vertex to add.
	 */
	public void addVertex(Vertex v) {
		vertices.put(v.getId(), v);
	}
	
	/**
	 * Removes vertex from the mesh.
	 * 
	 * @param v
	 *  		Vertex to remove.
	 * @return removed vertex.
	 */
	public Vertex removeVertex(Vertex v) {
		return vertices.remove(v.getId());
	}
	
	/**
	 * Remove list of vertices from the mesh.
	 * 
	 * @param vertices
	 *  		List of vertices to remove.
	 */
	public void removeAllVertices(List<Vertex> vertices) {
		vertices.removeAll(vertices);
	}

	/**
	 * Calculate all edges leading into or within the mesh.
	 */
	public void updateLE() {
		leadingToEdges.clear();
		for(Vertex v : vertices.values()) {
			v.setMeshNr(this.meshId);
			for(Edge e : v.getIE()) {
				leadingToEdges.add(e);
			}
		}
	}
	
	/**
	 * Checks whether this mesh is already to be split by another splitter.
	 * Synchronized to guarantee thread safety, set to true in same call if no splitter wants to split it yet.
	 * 
	 * @return true if other splitter wants to split already, false otherwise.
	 */
	public synchronized boolean toBeSplit() {
		if (toBeSplit)
			return true;
		else {
			toBeSplit = true;
			return false;
		}
	}
	
	/**
	 * Set whether mesh is already to be split by another splitter.
	 * 
	 * @param b
	 *  		Value toBeSplit to be set to.
	 */
	public synchronized void setToBeSplit(boolean b) {
		this.toBeSplit = b;
	}
}

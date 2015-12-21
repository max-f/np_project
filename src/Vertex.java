package npproject.improved.parallel;

import java.util.List;
import java.util.ArrayList;

/**
 * This class represents a Vertex Object.
 * 
 */
public class Vertex {
	
	private int id;
	private int capacity;
	private int meshNr;
	private List<Edge> incomingEdges;

	/**
	 * Constructor that has to be used to create a vertex object.
	 * 
	 * @param id Vertex ID.
	 * @param capacity Vertex capacity.
	 */
	public Vertex(int id, int capacity) {
		this.id = id;
		this.meshNr = 0;
		this.capacity = capacity;
		this.incomingEdges = new ArrayList<Edge>();
	}
	
	/**
	 * Returns the ID of a vertex.
	 * 
	 * @return Vertex ID.
	 */
	public int getId() {
		return id;
	}
	
	/**
	 * Adds one edge to the list of incoming edges.
	 * 
	 * @param e
	 *  		The edge to add.
	 */
	public void addIE(Edge e) {
		incomingEdges.add(e);
	}
	
	/**
	 * Returns the capacity of a vertex.
	 * 
	 * @return Vertex capacity.
	 */
	public int getCapacity() {
		return capacity; 
	}

	/**
	 * Returns the ID of a mesh containing this vertex.
	 * 
	 * @return ID of a mesh containing this vertex.
	 */
	public int getMeshNr() {
		return meshNr;
	}

	/**
	 * Determines the ID of containing mesh.
	 * 
	 * @param meshNr ID of a mesh containing this vertex.
	 */
	public void setMeshNr(int meshNr) {
		this.meshNr = meshNr;
	}

	/**
	 * Returns incoming edges.
	 * 
	 * @return A list of incoming edges.
	 */
	public List<Edge> getIE() {
		return incomingEdges;
	}
	
	@Override
	public boolean equals(Object o) {
		if(o instanceof Vertex && ((Vertex) o).getId() == this.id)
			return true;
		return false;
	}
}

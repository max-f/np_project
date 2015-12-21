package npproject.improved.parallel;

/**
 * This class represents an edge between two vertices.
 * 
 */
public class Edge {
	//id of the source Vertex
	private int srcId;
	//id of the target Vertex
	private int destId;
	
	/**
	 * Basic Constructor of an edge object.
	 */
	public Edge() {}
	
	/**
	 * Returns the ID of the source vertex.
	 * 
	 * @return ID of the source vertex.
	 */
	public int getSrcId() {
		return srcId;
	}
	
	/**
	 * Determines the source vertex.
	 * 
	 * @param id ID of the source Vertex.
	 */
	public void setSrcId(int id) {
		this.srcId = id;
	}
	
	/**
	 * Returns the ID of the target vertex.
	 * 
	 * @return ID of the target vertex.
	 */
	public int getDestId() {
		return destId;
	}
	
	/**
	 * Determines the target vertex.
	 * 
	 * @param id ID of the target Vertex.
	 */
	public void setDestId(int id) {
		this.destId = id;
	}
	
	@Override
	public boolean equals(Object o) {
		if(o  instanceof Edge && ((Edge) o).getDestId() == this.destId && ((Edge) o).getSrcId() == this.srcId)
			return true;
		return false;
	}

}

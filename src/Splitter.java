package npproject.improved.parallel;


import java.util.concurrent.*;
import java.util.List;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

/**
 * This class represents a splitter thread, which repetitive finds a mesh (M') and splits meshes (M), elements of Pre(M').
 * 
 * @implements It implements the interface Runnable as it is used for creating threads in Main.
 */
public class Splitter implements Runnable {
	private ConcurrentMap<Integer, Mesh> partition;
	private ConcurrentMap<Integer, Vertex> vertices;
	private CyclicBarrier barrier;
	private int threadCount;
	//list of meshes which should be remarked as unused as a splitter
	private List<Integer> uncheckSplitter = new ArrayList<Integer>();
	private static Integer inactiveThreads = 0;
	
	/**
	 * Constructs a Splitter with references to given parameters.
	 * 
	 * @param partition a reference to a map representing our main Partition (P) containing all meshes.
	 * @param vertices a reference to a map containing all vertices.
	 * @param barrier a reference to the controlling cyclic barrier in Main.
	 * @param threads a reference to an array of running threads which represent Splitters.
	 */
	public Splitter(ConcurrentMap<Integer, Mesh> partition, ConcurrentMap<Integer, Vertex> vertices, CyclicBarrier barrier, int threadCount) {
		this.partition = partition;
		this.vertices = vertices;
		this.barrier = barrier;
		this.threadCount = threadCount;
	}

	/**
	 * This method gets executed once a thread is started and represents the method of operation of a Splitter.
	 */
	public void run(){
		// All threads are being kept alive until all meshes are used as a splitter
		// If a thread can not find a mesh to use as splitter it is counted as inactive
		while(inactiveThreads < threadCount) {
			inactiveThreads = 0;
			// Obtain all pre-meshes with a list of vertices pointing to splitter
			Map<Integer, List<Vertex>> oneSplitter = checkOneSplitter();
			while(oneSplitter.size() != 0) {
				// This is the reading phase
				// If a thread obtains meshes to split it queues itself at barrier
				try {
					barrier.await();
				} catch (Exception e) {
					e.printStackTrace();
				}
				// This is the writing phase where we actually split the chosen meshes
				split(oneSplitter);
				// Wait until all threads are done with writing by waiting at barrier
				try {
					barrier.await();
				} catch (Exception e) {
				e.printStackTrace();
				}
				// Search for a new splitter mesh
				oneSplitter = checkOneSplitter();
			}
			// In case that a thread does not find any meshes to split it is considered as inactive
			// and is being queued at barrier to allow the active threads to advance
			try {
				barrier.await();
			} catch (Exception e) {
				e.printStackTrace();
			}
			// In case some pre-meshes of the last chosen splitters had conflicts with
			// other splitters, this splitter meshes have to be marked as still not finished.
			// Has to be done after the first barrier.
			for(Integer splitterId : uncheckSplitter) {
				partition.get(splitterId).setWasSplitter(false);
			}
			synchronized (inactiveThreads) {
				inactiveThreads++;
			}
			// It queues itself once again at barrier to allow all threads to go back
			// to the first phase
			try {
				barrier.await();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * This method searches for free meshes (M') which can be used as a splitter.
	 * It is called only in the reading (first) phase.
	 * 
	 * @return It returns a map of IDs of pre-meshes', which should be split,
	 * to a list of vertices in that pre-mesh that point to splitter mesh.
	 */
	public Map<Integer, List<Vertex>> checkOneSplitter() {
		uncheckSplitter.clear();
		// This is the map which will be returned
		Map<Integer, List<Vertex>> toSplit = new HashMap<Integer, List<Vertex>>();
		int i = 0;
		// Iterate over all meshes in search for a splitter mesh
		// size() does not have to be synchronized as we do not read and write simultaneous
		while (i < partition.size()) {
			Mesh m = partition.get(i);
			// getWasSplitter() is synchronized so each thread would take a different mesh
			// on which it would then calculate the image meshes
			if(m.getWasSplitter()) {
				i++;
				continue;
			}
			// if this mesh was already used as a splitter
			// but could not split all pre-meshes at previous time as those meshes were occupied
			// it only has to split those meshes left, which vertices, pointing to this splitter, are saved in toDo
			if(!m.getToDo().isEmpty()) {
				// here we collect all the id's of meshes and the vertices into toSplit map
				for(Vertex source : m.getToDo()) {
					int inverseImageId = source.getMeshNr();
					if(toSplit.containsKey(inverseImageId) && !toSplit.get(inverseImageId).contains(source)) {
						toSplit.get(inverseImageId).add(source);
					}
					else if(!toSplit.containsKey(inverseImageId)){
						List<Vertex> trueInverse = new ArrayList<Vertex>();
						trueInverse.add(source);
						toSplit.put(inverseImageId, trueInverse);
					}
				}
				m.clearToDo();
			}
			// here is the case of totally unused splitter
			else {
				// here we collect all the id's of meshes and the vertices into toSplit map
				for(Edge e : m.getLE()){
					Vertex source = vertices.get(e.getSrcId());
					int inverseImageId = source.getMeshNr();
					if(toSplit.containsKey(inverseImageId) && !toSplit.get(inverseImageId).contains(source)) {
						toSplit.get(inverseImageId).add(source);
					}
					else if(!toSplit.containsKey(inverseImageId)){
						List<Vertex> trueInverse = new ArrayList<Vertex>();
						trueInverse.add(source);
						toSplit.put(inverseImageId, trueInverse);
					}
				}
			}
			// Now we check which of those pre-meshes can not be split an take them out of toSplit
			Map<Integer, List<Vertex>> tempcopy = new HashMap<Integer, List<Vertex>>(toSplit);
			for(Integer meshId: toSplit.keySet()) {
				Mesh mesh = partition.get(meshId);
				Collection<Vertex> allVertices = mesh.getVertices().values();
				if(toSplit.get(meshId).containsAll(allVertices)){
					tempcopy.remove(meshId);
					continue;
				}
				// here we check which of those meshes are being split by some other splitter
				// we remove them from toSplit map and add their vertices in toDo list
				// we remark this mesh as unused as at least one of its splitable pre-meshes was not split
				// toBeSplit method is synchronized to avoid Data-Race
				if (mesh.toBeSplit()){
					m.addToDo(toSplit.get(meshId));
					tempcopy.remove(meshId);
					uncheckSplitter.add(m.getMeshId());
				}
			}
			toSplit = tempcopy;
			if(toSplit.size() != 0) {
				return toSplit;
			}
			i++;
		}
		return toSplit;
	}

	/**
	 * This method splits all given pre-meshes of one particular splitter.
	 * It is only called in the write (second) phase.
	 * 
	 * @param allInverseImages This a map of pre-mesh id's to a list of vertices
	 * that point to the splitter mesh.
	 */
	public void split(Map<Integer, List<Vertex>> allInverseImages) {
		// In case of conflicts, one pre-mesh has to be split by different splitters,
		// all splitters which can't split all their chosen pre-meshes in this iteration have to be remarked as unfinished
		for(Integer splitterId : uncheckSplitter) {
			partition.get(splitterId).setWasSplitter(false);
		}
		// Split a target mesh into two meshes:
		// One new mesh that contains all vertices that were pointing to the splitter mesh
		// The other mesh is the existing mesh from which we remove those vertices
		for(int meshId : allInverseImages.keySet()) {
			Mesh actualMesh = partition.get(meshId);
			Mesh newMesh = new Mesh(actualMesh.getCapacity());
			// As we assign partitions size as a mesh id of the new mesh
			// we have to synchronize this process to avoid several meshes having the same id
			// After this process the partition size has changed by the put action
			synchronized (partition) {
				int newMeshId = partition.size();
				partition.put(newMeshId, newMesh);
				newMesh.setMeshId(newMeshId);
			}
			// Remove the corresponding vertices from target mesh into the new mesh
			for(Vertex v : allInverseImages.get(meshId)) {
				newMesh.addVertex(actualMesh.removeVertex(v));
			}
			// Update those meshes and mark as unused
			newMesh.updateLE();
			actualMesh.updateLE();
			actualMesh.setWasSplitter(false);
			actualMesh.setToBeSplit(false);
			actualMesh.clearToDo();
		}
	}
}
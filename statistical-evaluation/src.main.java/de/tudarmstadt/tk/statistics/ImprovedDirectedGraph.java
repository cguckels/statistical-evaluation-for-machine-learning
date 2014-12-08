package de.tudarmstadt.tk.statistics;

/**
 * Copyright 2014
 * Telecooperation (TK) Lab
 * Technische Universität Darmstadt
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

/**
 * Extends the DefaultDirectedGraph from the JGraphT library with capabilities to do a deep copy (clone) of a directed graph and to calculate the topological order of nodes with their levels
 * @author Guckelsberger, Schulz
 *
 * @param <V> The type of vertices 
 * @param <E> The type of edges
 */
public class ImprovedDirectedGraph<V,E> extends DefaultDirectedGraph<V,E>{

	private static final long serialVersionUID = 1L;

	public ImprovedDirectedGraph(Class edgeClass) {
		super(edgeClass);
	}

	/**
	 * Creates a deep copy of this directed graph, including vertices and edges
	 */
	public ImprovedDirectedGraph<V,E> clone(){
		ImprovedDirectedGraph<V, E> dga = new ImprovedDirectedGraph<V, E>((Class<? extends E>) DefaultEdge.class);
		for(V n:this.vertexSet()){
			dga.addVertex(n);
		}
		for(E e:this.edgeSet()){
			V n1=this.getEdgeSource(e);
			V n2=this.getEdgeTarget(e);
			dga.addEdge(n1, n2);
		}
		return dga;
	}
	
	/**
	 * Calculates the topological order of this graph's nodes. In contrast to the JGraphT-internal {@link TopologicalIterator}, this method returns nodes on the same levels as group with an indicator of the level 
	 * @return A HashMap mapping from level indicators to all nodes on that level
	 */
	public HashMap<Integer,TreeSet<V>> getTopologicalOrder(){
		
		ImprovedDirectedGraph<V,E> dg = this.clone();
		
		//Empty list that will contain the sorted elements
		HashMap<Integer,TreeSet<V>> sorted = new HashMap<Integer,TreeSet<V>>();
		
		//Determine set of all nodes with no incoming edges
		TreeSet<V> targetVertices = new TreeSet<V>();
		Iterator<E> it = dg.edgeSet().iterator();
		while(it.hasNext()){
			targetVertices.add((V) dg.getEdgeTarget(it.next()));
		}
		TreeSet<V> freeVertices = new TreeSet<V>(dg.vertexSet());
		freeVertices.removeAll(targetVertices);
		
		TreeSet<V> nextFreeVertices = new TreeSet<V>();
		//While S is non-empty do
		int level=0;
		sorted.put(level, new TreeSet<V>());
		while(!freeVertices.isEmpty() || !nextFreeVertices.isEmpty()){
			
			if(freeVertices.isEmpty()){
				freeVertices.addAll(nextFreeVertices);
				nextFreeVertices.clear();
				level++;
				sorted.put(level, new TreeSet<V>());
			}
			
			//Remove a node n from S
			V n = freeVertices.first();
			freeVertices.remove(n);
			//Add n to tail of L
			sorted.get(level).add(n);
			
			//For each node m with an edge e from n to m do			
			List<E> outgoing = new ArrayList<E>(dg.outgoingEdgesOf(n));
			for(E e:outgoing){
				//Remove edge e from the graph
				V m = (V) dg.getEdgeTarget(e);
				dg.removeEdge(e);
		        //If m has no other incoming edges then insert m into S
				if(dg.inDegreeOf(m)==0){
					nextFreeVertices.add(m);
				}
			}
		}
		
		if(dg.edgeSet().size()>0){
			System.err.println("Graph has at least one cycle. Returning null.");
			return null;
		}
		return sorted;
	}
	
	
	
}

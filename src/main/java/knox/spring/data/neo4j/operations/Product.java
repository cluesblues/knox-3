package knox.spring.data.neo4j.operations;

import knox.spring.data.neo4j.domain.Edge;
import knox.spring.data.neo4j.domain.Node;
import knox.spring.data.neo4j.domain.NodeSpace;
import knox.spring.data.neo4j.domain.Node.NodeType;

import java.util.*;

public class Product {
	private List<Node> rowNodes;
	
	private List<Node> colNodes;
	
//	private List<List<Node>> productNodes;
//	
//	private HashMap<String, Integer> idToRowIndex = new HashMap<String, Integer>();
//	
//	private HashMap<String, Integer> idToColIndex = new HashMap<String, Integer>();
	
	private HashMap<Integer, Set<Node>> rowToProductNodes;
	
	private HashMap<Integer, Set<Node>> colToProductNodes;
	
	private NodeSpace productSpace;
	
	public Product(NodeSpace rowSpace, NodeSpace colSpace) {
		productSpace = new NodeSpace();
		
		this.rowNodes = rowSpace.orderNodes();
		
		this.colNodes = colSpace.orderNodes();
		
		rowToProductNodes = new HashMap<Integer, Set<Node>>();
		
		colToProductNodes = new HashMap<Integer, Set<Node>>();
		
//		productNodes = new ArrayList<List<Node>>(rowNodes.size());
//		
//		idToRowIndex = new HashMap<String, Integer>();
//		
//		idToColIndex = new HashMap<String, Integer>();
    	
//    	for (int i = 0; i < rowNodes.size(); i++) {
//			productNodes.add(new ArrayList<Node>(colNodes.size()));
//
//			for (int j = 0; j < colNodes.size(); j++) {
//				Node node;
//				
//				if (rowNodes.get(i).isAcceptNode() || colNodes.get(j).isAcceptNode()) {
//					node = productSpace.createAcceptNode();
//					
//					productNodes.get(i).add(node);
//				} else if (rowNodes.get(i).isStartNode() && colNodes.get(j).isStartNode()) {
//					node = productSpace.createStartNode();
//					
//					productNodes.get(i).add(node);
//				} else {
//					node = productSpace.createNode();
//					
//					productNodes.get(i).add(node);
//				}
//				
//				idToRowIndex.put(node.getNodeID(), new Integer(i));
//				
//				idToColIndex.put(node.getNodeID(), new Integer(j));
//			}
//		}
	}
	
	public void connect(String type, int tolerance, boolean isModified) {
		if (type.equals(ProductType.TENSOR.getValue())) {
			if (isModified) {
				modifiedTensor(tolerance);
			} else {
				tensor(tolerance);
			}
		} else if (type.equals(ProductType.CARTESIAN.getValue())) {
			cartesian();
		} else if (type.equals(ProductType.STRONG.getValue())) {
			if (isModified) {
				modifiedStrong(tolerance);
			} else {
				strong(tolerance);
			}
		}
	}
	

    public NodeSpace getProductSpace() {
    	return productSpace;
    }
    
    public List<Set<Edge>> cartesian() {
    	for (int i = 0; i < rowNodes.size(); i++) {
    		for (int j = 0; j < colNodes.size(); j++) {
    			crossNodes(i, j);
    		}
    	}
    	
    	List<Set<Edge>> orthogonalEdges = new ArrayList<Set<Edge>>(2);
    	
    	orthogonalEdges.add(new HashSet<Edge>());

		for (int i = 0; i < rowNodes.size(); i++) {
			if (rowNodes.get(i).hasEdges()) {
				for (Edge rowEdge : rowNodes.get(i).getEdges()) {
					int r = locateNode(rowEdge.getHead(), i, rowNodes);

					for (int j = 0; j < colNodes.size(); j++) {
						orthogonalEdges.get(0).add(getProductNode(i, j).copyEdge(rowEdge, 
								getProductNode(r, j)));
					}
				}
			}
		}

		orthogonalEdges.add(new HashSet<Edge>());

		for (int j = 0; j < colNodes.size(); j++) {
			if (colNodes.get(j).hasEdges()) {
				for (Edge colEdge : colNodes.get(j).getEdges()) {
					int c = locateNode(colEdge.getHead(), j, colNodes);

					for (int i = 0; i < rowNodes.size(); i++) {
						orthogonalEdges.get(1).add(getProductNode(i, j).copyEdge(colEdge, 
								getProductNode(i, c)));
					}
				}
			}
		}
		
		return orthogonalEdges;
    }
    
    private Set<Node> projectRow(int i, HashMap<Integer, Node> rowToProductNode) {
    	Set<Node> productNodes;

		if (rowToProductNodes.containsKey(i)) {
			productNodes = rowToProductNodes.get(i);
		} else {
			productNodes = new HashSet<Node>();

			if (rowToProductNode.containsKey(i)) {
				productNodes.add(rowToProductNode.get(i));
			} else {
				Node productNode = productSpace.copyNode(rowNodes.get(i));

				rowToProductNode.put(i, productNode);

				productNodes.add(productNode);
			}
		}
		
		return productNodes;
    }
    
    private Set<Node> projectColumn(int j, HashMap<Integer, Node> colToProductNode) {
    	Set<Node> productNodes;

		if (colToProductNodes.containsKey(j)) {
			productNodes = colToProductNodes.get(j);
		} else {
			productNodes = new HashSet<Node>();

			if (colToProductNode.containsKey(j)) {
				productNodes.add(colToProductNode.get(j));
			} else {
				Node productNode = productSpace.copyNode(colNodes.get(j));

				colToProductNode.put(j, productNode);

				productNodes.add(productNode);
			}
		}
		
		return productNodes;
    }
    
    public void modifiedStrong(int tolerance) {
    	tensor(tolerance);
    	
    	productSpace.deleteUnconnectedNodes();
    	
    	HashMap<Integer, Node> rowToProductNode = new HashMap<Integer, Node>();

    	for (int i = 0; i < rowNodes.size(); i++) {
    		if (rowNodes.get(i).hasEdges()) {
    			Set<Node> productNodes = projectRow(i, rowToProductNode);
    			
    			for (Edge rowEdge : rowNodes.get(i).getEdges()) {
    				int r = locateNode(rowEdge.getHead(), i, rowNodes);

    				Set<Node> productHeads = projectRow(r, rowToProductNode);

    				for (Node productNode : productNodes) {
    					for (Node productHead : productHeads) {
    						productNode.copyEdge(rowEdge, productHead);
    					}
    				}
    			}
    		}
    	}

    	HashMap<Integer, Node> colToProductNode = new HashMap<Integer, Node>();

    	for (int j = 0; j < colNodes.size(); j++) {
    		if (colNodes.get(j).hasEdges()) {
    			Set<Node> productNodes = projectColumn(j, colToProductNode);
    			
    			for (Edge colEdge : colNodes.get(j).getEdges()) {
    				int c = locateNode(colEdge.getHead(), j, colNodes);

    				Set<Node> productHeads = projectColumn(c, colToProductNode);

    				for (Node productNode : productNodes) {
    					for (Node productHead : productHeads) {
    						productNode.copyEdge(colEdge, productHead);
    					}
    				}
    			}
    		}
    	}
    	
    	if (productSpace.getStartNodes().size() > 1) {
    		Union union = new Union(productSpace);
    		
    		if (productSpace.isConnected()) {
    			union.connect(false);
    		} else {
    			union.connect(true);
    		}
    	}
    }
    
    public void modifiedTensor(int tolerance) {
    	tensor(tolerance);
    	
    	productSpace.labelSourceNodesStart();
    	
    	productSpace.labelSinkNodesAccept();
    	
    	if (productSpace.getStartNodes().size() > 1) {
    		Union union = new Union(productSpace);
    		
    		if (productSpace.isConnected()) {
    			union.connect(false);
    		} else {
    			union.connect(true);
    		}
    	}
    }
    
    public void strong(int tolerance) {
    	tensor(tolerance);
    	
    	cartesian();
    }
    
    public void tensor(int tolerance) {
    	for (int i = 0; i < rowNodes.size(); i++) {
    		for (int j = 0; j < colNodes.size(); j++) {
    			if (rowNodes.get(i).hasEdges() 
    					&& colNodes.get(j).hasEdges()) {
    				for (Edge rowEdge : rowNodes.get(i).getEdges()) {
    					for (Edge colEdge : colNodes.get(j).getEdges()) {
    						if (rowEdge.isMatchingTo(colEdge, tolerance)) {
    							if (!hasProductNode(i, j)) {
    								crossNodes(i, j);
    							}

    							int r = locateNode(rowEdge.getHead(), i, 
    									rowNodes);

    							int c = locateNode(colEdge.getHead(), j,
    									colNodes);
    							
    							if (!hasProductNode(r, c)) {
    								crossNodes(r, c);
    							}
    							
    							Edge productEdge = getProductNode(i, j).copyEdge(colEdge, 
    									getProductNode(r, c));

    							if (tolerance == 0 || tolerance > 1 && tolerance <= 4) {
    								productEdge.unionWithEdge(rowEdge);
    							} else if (tolerance == 1) {
    								productEdge.intersectWithEdge(rowEdge);
    							}
    						}
    					}
    				}
    			}
    		}
    	}
    }
    
    private void addProductNode(int i, int j, Node productNode) {
    	if (!rowToProductNodes.containsKey(i)) {
    		rowToProductNodes.put(i, new HashSet<Node>());
    	} 
    	
    	rowToProductNodes.get(i).add(productNode);
    	
    	if (!colToProductNodes.containsKey(j)) {
    		colToProductNodes.put(j, new HashSet<Node>());
    	}
    	
    	colToProductNodes.get(j).add(productNode);
    }
    
    private void crossNodes(int i, int j) {
    	if (!hasProductNode(i, j)) {
			Node productNode = productSpace.createNode();
			
			if (rowNodes.get(i).isStartNode() && colNodes.get(j).isStartNode()) {
				productNode.addNodeType(NodeType.START.getValue());
			} else if (rowNodes.get(i).isAcceptNode() || colNodes.get(j).isAcceptNode()) {
				productNode.addNodeType(NodeType.ACCEPT.getValue());
			}
			
			addProductNode(i, j, productNode);
		}
    }
   
    private Node getProductNode(int i, int j) {
    	if (rowToProductNodes.containsKey(i) && colToProductNodes.containsKey(j)) {
    		Set<Node> productNodes = new HashSet<Node>(rowToProductNodes.get(i));
    		
        	productNodes.retainAll(colToProductNodes.get(j));
        	
        	return productNodes.iterator().next();
    	} else {
    		return null;
    	}
    }
    
    private boolean hasProductNode(int i, int j) {
    	if (rowToProductNodes.containsKey(i) && colToProductNodes.containsKey(j)) {
    		Set<Node> productNodes = new HashSet<Node>(rowToProductNodes.get(i));
    		
        	productNodes.retainAll(colToProductNodes.get(j));
        	
        	return productNodes.size() == 1;
    	} else {
    		return false;
    	}
    }
    
    private int locateNode(Node node, int k, List<Node> nodes) {
    	for (int i = k; i < nodes.size(); i++) {
    		if (nodes.get(i).isIdenticalTo(node)) {
    			return i;
    		}
    	}
    	
    	for (int i = 0; i < k; i++) {
    		if (nodes.get(i).isIdenticalTo(node)) {
    			return i;
    		}
    	}
    	
    	return -1;
    }
    
    public enum ProductType {
        TENSOR("tensor"),
        CARTESIAN("cartesian"),
        STRONG("strong");

        private final String value;

        ProductType(String value) { 
        	this.value = value;
        }

        public String getValue() {
        	return value; 
        }
    }
}

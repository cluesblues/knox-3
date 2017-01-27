package knox.spring.data.neo4j.eugene;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

public class Ruleset implements Comparator<Ruleset> {
	
	private Part implicant;
	
	private Set<Part> coImplicants;
	
	private Set<Part> implied;
	
	private Set<Part> weaklyImplied;
	
	private int rank = -1;
	
	public Ruleset() {
		
	}
	
	public Ruleset(Part implicant) {
		this.implicant = implicant;
		
		this.coImplicants = new HashSet<Part>();
		
		this.implied = new HashSet<Part>();
		
		this.weaklyImplied = new HashSet<Part>();
	}
	
	public void addRule(Rule rule) {
		if (rule.isAdjacencyRule()) {

		} else if (rule.isNonStrictPrecedenceRule()) {
			if (implicant.isIdenticalTo(rule.getImplicant())) {
				weaklyImplied.add(rule.getImplied());
			} else {
				coImplicants.add(rule.getImplicant());
			}
		} else if (rule.isStrictPrecedenceRule()) {
			implied.add(rule.getImplied());
		}
	}
	
	public int compareTo(Ruleset r) {
		return this.compareTo(r);
	}

	public int compare(Ruleset r1, Ruleset r2) {
		return r1.rank - r2.rank;
	}
	
	public Set<Part> getImplied() {
		return implied;
	}
	
	public Set<Part> getCoImplicants() {
		return coImplicants;
	}
	
	public Part getImplicant() {
		return implicant;
	}
	
	public Set<Part> getWeaklyImplied() {
		return weaklyImplied;
	}
	
	public boolean hasRank() {
		return rank >= 0;
	}
	
	public void setRank(int rank) {
		this.rank = rank;
	}
}

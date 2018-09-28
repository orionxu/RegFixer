package utilities;

import java.io.Serializable;


public class Triple<A, B, C> implements Serializable{

	private static final long serialVersionUID = 6686660062988122937L;
	
	public A first;
	public B second;
	public C third;

    protected Triple(){}
    
    public Triple(A first, B second, C third) {
    	super();
    	this.first = first;
    	this.second = second;
    	this.third = third;
    }

    

    @Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((first == null) ? 0 : first.hashCode());
		result = prime * result + ((second == null) ? 0 : second.hashCode());
		result = prime * result + ((third == null) ? 0 : third.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Triple<?, ?, ?> other = (Triple<?, ?, ?>) obj;
		if (first == null) {
			if (other.first != null)
				return false;
		} else if (!first.equals(other.first))
			return false;
		if (second == null) {
			if (other.second != null)
				return false;
		} else if (!second.equals(other.second))
			return false;
		if (third == null) {
			if (other.third != null)
				return false;
		} else if (!third.equals(other.third))
			return false;
		return true;
	}

	public String toString()
    { 
           return "(" + first + ", " + second + ", " + third + ")"; 
    }

}
package astar;

import java.util.ArrayList;



public class GridBlock {
    int x, y; 
    
    int row;   
	int col;   
    int g;     
    int h;     
    int f;     
    int dist;  
    GridBlock prev; 
  
    public GridBlock(int row, int col){
       this.row = row;
       this.col = col;
    }
    
    ArrayList<GridBlock> neighbors = new ArrayList<>();
    boolean wall = true;
    boolean open = true;
    
    GridBlock(int x, int y, boolean isWall) {
        this.x = x;
        this.y = y;
        this.wall = isWall;
    }
    void addNeighbor(GridBlock other) {
        if (!this.neighbors.contains(other)) { 
            this.neighbors.add(other);
        }
        if (!other.neighbors.contains(this)) { 
            other.neighbors.add(this);
        }
    }
    boolean isGridBlockBelowNeighbor() {
        return this.neighbors.contains(new GridBlock(this.x, this.y + 1));
    }
    boolean isGridBlockRightNeighbor() {
        return this.neighbors.contains(new GridBlock(this.x + 1, this.y));
    }
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof GridBlock)) return false;
        GridBlock otherGridBlock = (GridBlock) other;
        return (this.x == otherGridBlock.x && this.y == otherGridBlock.y);
    }

    @Override
    public int hashCode() {
        return this.x + this.y * 256;
    }
    
    /**
	 * @return the row
	 */
	public int getRow() {
		return row;
	}
	/**
	 * @param row the row to set
	 */
	public void setRow(int row) {
		this.row = row;
	}
	/**
	 * @return the col
	 */
	public int getCol() {
		return col;
	}
	/**
	 * @param col the col to set
	 */
	public void setCol(int col) {
		this.col = col;
	}
	/**
	 * @return the g
	 */
	public int getG() {
		return g;
	}
	/**
	 * @param g the g to set
	 */
	public void setG(int g) {
		this.g = g;
	}
	/**
	 * @return the h
	 */
	public int getH() {
		return h;
	}
	/**
	 * @param h the h to set
	 */
	public void setH(int h) {
		this.h = h;
	}
	/**
	 * @return the f
	 */
	public int getF() {
		return f;
	}
	/**
	 * @param f the f to set
	 */
	public void setF(int f) {
		this.f = f;
	}
	/**
	 * @return the dist
	 */
	public int getDist() {
		return dist;
	}
	/**
	 * @param dist the dist to set
	 */
	public void setDist(int dist) {
		this.dist = dist;
	}

    

}
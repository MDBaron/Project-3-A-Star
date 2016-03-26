package astar;

import java.util.ArrayList;
import java.util.Random;

import astar.GridBlock;
import astar.StarPanel;

public class GridGenerator {
	
        private int sizeX, sizeY;
        private int boardsizeX, boardsizeY; 
        private char[][] Gridboard; 
        private GridBlock[][] GridBlocks;
        private Random random = new Random();
        StarPanel sp;

        
        public GridGenerator(int asize,StarPanel sp) {
            //this pushes the single values to the second constructor to guarantee a square board
            this(asize, asize, sp);
            this.sp = sp;
        }
        
        public GridGenerator(int xsize, int ysize, StarPanel globalSp) {
            sizeX = xsize;
            sizeY = ysize;
            boardsizeX = xsize * 2 + 1;
            boardsizeY = ysize * 2 + 1;
            Gridboard = new char[boardsizeX][boardsizeY];
            init();
            generateGrid();
            this.sp = globalSp;
        }

        private void init() {
            
            GridBlocks = new GridBlock[sizeX][sizeY];
            for (int x = 0; x < sizeX; x++) {
                for (int y = 0; y < sizeY; y++) {
                    GridBlocks[x][y] = new GridBlock(x, y, false); 
                }
            }
        }

       
        private class GridBlock {
            int x, y; 
            
            ArrayList<GridBlock> neighbors = new ArrayList<>();
            boolean obstacle = true;
            boolean open = true;
            GridBlock(int x, int y) {
                this(x, y, true);
            }
            GridBlock(int x, int y, boolean isobstacle) {
                this.x = x;
                this.y = y;
                this.obstacle = isobstacle;
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

        }
        private void generateGrid() {
            generateGrid(0, 0);
        }
        private void generateGrid(int x, int y) {
            generateGrid(getGridBlock(x, y));
        }
        private void generateGrid(GridBlock startAt) {
            if (startAt == null) return;
            startAt.open = false; 
            ArrayList<GridBlock> GridBlocksList = new ArrayList<>();
            GridBlocksList.add(startAt);

            while (!GridBlocksList.isEmpty()) {
                GridBlock GridBlock;
               
                
                if (random.nextInt(10)==0)
                    GridBlock = GridBlocksList.remove(random.nextInt(GridBlocksList.size()));
                else GridBlock = GridBlocksList.remove(GridBlocksList.size() - 1);
             
                //Push neighbours into an array to check later 
                ArrayList<GridBlock> neighbors = new ArrayList<>();
                GridBlock[] potentialNeighbors = new GridBlock[]{
                    getGridBlock(GridBlock.x + 1, GridBlock.y),
                    getGridBlock(GridBlock.x, GridBlock.y + 1),
                    getGridBlock(GridBlock.x - 1, GridBlock.y),
                    getGridBlock(GridBlock.x, GridBlock.y - 1)
                };
                
                //check against current GridBlock object for obstacle or open
                for (GridBlock other : potentialNeighbors) {
                    if (other==null || other.obstacle || !other.open) continue;
                    neighbors.add(other);
                }
                //skip if empty, otherwise randomly select a neighbor close
                if (neighbors.isEmpty()) continue;
                GridBlock selected = neighbors.get(random.nextInt(neighbors.size()));
                selected.open = false;
                GridBlock.addNeighbor(selected);
                GridBlocksList.add(GridBlock);
                GridBlocksList.add(selected);
            }
            updateboard();
        }
        //return the GridBlock array
        public GridBlock getGridBlock(int x, int y) {
            try {
                return GridBlocks[x][y];
            } catch (ArrayIndexOutOfBoundsException e) { 
                return null;
            }
        }
        //update board based on field/file input sizes and GridBord contents 
        public void updateboard() {
            char backChar = ' ', obstacleChar = 'o', GridBlockChar = 'e';
            for (int x = 0; x < boardsizeX; x ++) {
                for (int y = 0; y < boardsizeY; y ++) {
                    Gridboard[x][y] = backChar;
                }
            }
            for (int x = 0; x < boardsizeX; x ++) {
                for (int y = 0; y < boardsizeY; y ++) {
                    if (x % 2 == 0 || y % 2 == 0)
                        Gridboard[x][y] = obstacleChar;
                }
            }
            for (int x = 0; x < sizeX; x++) {
                for (int y = 0; y < sizeY; y++) {
                    GridBlock current = getGridBlock(x, y);
                    int boardX = x * 2 + 1, boardY = y * 2 + 1;
                    Gridboard[boardX][boardY] = GridBlockChar;
                    if (current.isGridBlockBelowNeighbor()) {
                        Gridboard[boardX][boardY + 1] = GridBlockChar;
                    }
                    if (current.isGridBlockRightNeighbor()) {
                        Gridboard[boardX + 1][boardY] = GridBlockChar;
                    }
                }
            }
            
            sp.searching = false;
            sp.endOfSearch = false;
            Driver.globalSp.fillboard();
            for (int x = 0; x < boardsizeX; x++) {
                for (int y = 0; y < boardsizeY; y++) {
                    if (Gridboard[x][y] == obstacleChar && Driver.globalSp.board[x][y] != Driver.globalSp.getPath() && Driver.globalSp.board[x][y] != Driver.globalSp.getEnd()){
                    	Driver.globalSp.board[x][y] = Driver.globalSp.getObst();
                    }
                }
            }
        }
    
}

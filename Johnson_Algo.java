/*
Code Johnson's algorithm.  

Input:
an integer v < 100 designating the number of vertices in the directed graph 
followed by v lines of v integers each, representing the adjacency matrix.

output:
a v x v matrix representing all pairs shortest paths.
 */
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

/**
 *
 * @author Carl Smith
 */
class Vertex {
    public int id;
    public int weight;
    public int d;
    public ArrayList<Neighbor> neighbors;
    
    public Vertex(int id){
        this.id = id;
        this.weight = 0;
        this.d = Integer.MAX_VALUE; //substitute for infinity
        this.neighbors = new ArrayList<>();
    }
    
    public boolean isEqual(Vertex v){
        if(v == null)
            return false;
        return v.id == this.id;
    }
};

class Neighbor {
    public int id;
    public int weight;
    
    public Neighbor(int id, int w){
        this.id = id;
        this.weight = w;
    }
};

public class Johnson_Algo {
    
    public static ArrayList<Vertex> fileToArray(String filename) {
        ArrayList<Vertex> arr = new ArrayList<>();
        Scanner inputF;
        int col = 0;
        int row = 0;
        int w = 0;
        int v = 0;
        try {
            inputF = new Scanner(new File(filename));
            while (inputF.hasNextInt()) {
                w = inputF.nextInt();
                if(col == 0 && row == 0){   //if first run
                    v = w;
                    col++;
                    col = v; //to create new row
                }
                else if(col == v){  //if new row
                    col = 1;
                    row++;
                    arr.add(new Vertex(row));
                    if(w == 0)
                        w = Integer.MIN_VALUE;
                    arr.get(row-1).neighbors.add(new Neighbor(col,w));
                }
                else{
                    col++;
                    if(w == 0)
                        w = Integer.MIN_VALUE;
                    arr.get(row-1).neighbors.add(new Neighbor(col,w));    
                }
            }
            inputF.close();
        } catch (FileNotFoundException ex) {
            System.out.println("File \"" + filename + "\" not found.");
        }
        return arr;
    }
    
    public static void displayMatrix(ArrayList<Integer> D, int size){
        for(int i = 0; i < size; i++){
            for(int j = 0; j < size; j++){
                int w = D.get(i*size + j);
                if(w == Integer.MAX_VALUE) //if it equals 'infinity'
                    System.out.print("inf\t");
                else
                    System.out.print(w + "\t");
            }
            System.out.println();
        }
    }
    
    public static void displayMatrix(ArrayList<Vertex> G){
        for(Vertex u : G){
            for(Neighbor n : u.neighbors){
                if(n.weight == Integer.MIN_VALUE)
                    System.out.print("inf\t");
                else
                    System.out.print(n.weight+"\t");
            }
            System.out.println();
        }
        System.out.println();
    }
    
    public static void primeG(ArrayList<Vertex> G){
        Vertex u = new Vertex(0);
        for(int i = 0; i < G.size(); i++){
            u.neighbors.add(new Neighbor(G.get(i).id, 0));
        }
        G.add(0, u);
    }
    
    public static void unprimeG(ArrayList<Vertex> Gp){
        Gp.remove(0);
    }
    
    public static boolean BellmanFord(ArrayList<Vertex> Gp, Vertex s){
        //initialize-single-source: d and pi values already initialized for vertices
        s.d = 0;
        //relaxation
        for (Vertex u : Gp){
            for(Neighbor n : u.neighbors){
                Vertex v = Gp.get(n.id);    //vertices have ids with values one more than array index
                if(v.isEqual(u) || n.weight == Integer.MIN_VALUE) //skip if vertex is same or wrong direction
                    continue;
                if(v.d > u.d + n.weight){
                    v.d = u.d + n.weight;
                }
            }
        }
        //find cycle
        for (Vertex u : Gp){
            for(Neighbor n : u.neighbors){
                Vertex v = Gp.get(n.id);    //vertices have ids with values one more than array index
                if(v.isEqual(u) || n.weight == Integer.MIN_VALUE)   //skip if vertex is same or wrong direction
                    continue; 
                if(v.d > u.d + n.weight){
                    return false;
                }
            }
        }
        return true;
    }
    
    public static void Dijkstra(ArrayList<Vertex> G, Vertex s){
        //initialize-single-source
        for(Vertex v : G){
            v.d = Integer.MAX_VALUE;
        }
        s.d = 0;
        //create array
        ArrayList<Vertex> Q = new ArrayList<>(G);
        Collections.swap(Q, 0, s.id-1); //put s vertex in front     
        Vertex u = null;
        int remove = -1;
        //min extraction and relaxation 
        while(!Q.isEmpty()){    //while Q is not empty extract vector with min v.d
            for(int i = 0; i < Q.size(); i++){
                Vertex v = Q.get(i);
                if(i == 0 || v.d <= u.d || Q.size() == 1){
                    u = v;
                    remove = i;
                } 
            }
            if(remove != -1){
                Q.remove(remove);
                remove = -1;
            }
            for(Neighbor n : u.neighbors){      //relax
                Vertex v = G.get(n.id - 1);     //vertices have ids with values one more than array index
                if(v.isEqual(u) || n.weight == Integer.MIN_VALUE ||
                        u.d == Integer.MAX_VALUE) //skip if vertex is same or wrong direction or has max_value(infinity)
                    continue;
                if(v.d > u.d + n.weight){
                    v.d = u.d + n.weight;
                }
            }
        }
    }
    
    public static ArrayList<Integer> Johnson(ArrayList<Vertex> G){
        ArrayList<Integer> D = new ArrayList<>();
        ArrayList<Integer> h = new ArrayList<>();
        
        //create G' with a new vertex which has all other vertices as neighbors
        primeG(G);
        //determine if a negative-weight cycle exists
        if(!BellmanFord(G,G.get(0))){  
            return null;    
        }
        else{
            //remove s and revert G' to G
            unprimeG(G); 
            //save h(v) values
            for(Vertex v : G){
                h.add(v.d);
            }
            //reweight edge-connections
            for (Vertex u : G){ 
                for (Neighbor n : u.neighbors){
                    Vertex v = G.get(n.id-1);    //vertices have ids with values one more than array index
                    if(v.isEqual(u) || n.weight == Integer.MIN_VALUE) //skip if same edge or wrong direction
                        continue;
                    n.weight = n.weight + u.d - v.d;
                }
            }
            //run Dijkstra's algorithm for each vertex in G
            for(Vertex u : G){
                Dijkstra(G, u);
                for (Vertex v : G){
                    int x;
                    if(v.d == Integer.MAX_VALUE)
                        x = v.d;
                    else
                        x = v.d + h.get(v.id-1) - h.get(u.id-1);
                    D.add(x);
                }
            }
        }
        return D;
    }
    
    public static void main(String[] args) {
        ArrayList<Vertex> G = fileToArray("matrix.txt");
        int size = G.size();
        
        //displayMatrix(G);  //TEST: SEE ADJACENCY MATRIX
        ArrayList<Integer> D = Johnson(G);
        if(D == null)
            System.out.println("The graph contains a negative-weight cycle.");
        else
            displayMatrix(D, size);
    }
    
}
 
package arkhados;

import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.terrain.heightmap.AbstractHeightMap;
import com.jme3.terrain.heightmap.ImageBasedHeightMap;
import com.jme3.texture.Texture;
import com.jme3.util.BufferUtils;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Ilkka
 */


public final class Grid extends Mesh {
    private int _sizeX;
    private int _sizeY;
    private float _density;

    public Grid(int sizeX, int sizeY, float scale){
        updateGeometry(sizeX, sizeY, scale);        
    }
    
    public void updateGeometry(int sizeX, int sizeY, float density){
        _sizeX = sizeX;
        _sizeY = sizeY;
        _density = density;
        Vector3f[] vertices = CreateVertices();
        Vector3f[] normals = CreateNormals();
        Vector2f[] texCoords = CreateTexCoords();
        int[] indices = CreateIndices();
        setBuffer(Type.Position, 3, BufferUtils.createFloatBuffer(vertices));        
        setBuffer(Type.TexCoord, 2, BufferUtils.createFloatBuffer(texCoords));
        setBuffer(Type.Normal, 3, BufferUtils.createFloatBuffer(normals));
        setBuffer(Type.Index, 3, indices);
        
        updateBound();
        setStatic();
    }
    
    private Vector3f[] CreateNormals(){
        Vector3f[] result = new Vector3f[_sizeX*_sizeY];
        for (int i = 0; i < result.length; i++)
        {
            result[i] = Vector3f.UNIT_Y;
        }
        return result;
    }
    
    private Vector3f[] CreateVertices(){
        Vector3f[] result = new Vector3f[_sizeX*_sizeY];       
        for (int x = 0; x < _sizeX; x++){
            for (int y = 0; y < _sizeY; y++){
                result[x + y * _sizeY] = new Vector3f((float)x * _density,0, (float)y * _density);
            }
        }
        
        return result;
    }
    
    private Vector2f[] CreateTexCoords(){
        Vector2f[] result = new Vector2f[_sizeX*_sizeY];
        
        for (float x = 0; x < _sizeX; x++){
            for (float y = 0; y < _sizeY; y++){
                result[(int)x + (int)y *_sizeY] = new Vector2f(x/(float)_sizeX*4f, y/ (float)_sizeY*4f);
            }
        }
        
        return result;
    }
    
    private int[] CreateIndices(){
        List<Integer> resultList = new ArrayList<>();

        
        for (int x = 0; x < _sizeX; x++){
            for (int y = 0; y < _sizeY; y++){
                Integer id = (int)(x + y * _sizeY);
                resultList.add(id + _sizeY);
                resultList.add(id + 1);                
                resultList.add(id);
                
                resultList.add(id + _sizeY);
                resultList.add(id + _sizeY + 1);                
                resultList.add(id + 1);
            }
        }
        int[] result = new int[resultList.size()];
        
        for (int i = 0; i < resultList.size(); ++i) { 
                result[i] = resultList.get(i); 
        }
        
        return result;
    }
}

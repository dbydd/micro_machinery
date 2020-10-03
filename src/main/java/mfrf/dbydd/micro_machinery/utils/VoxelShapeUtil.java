package mfrf.dbydd.micro_machinery.utils;

import net.minecraft.block.Block;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.shapes.VoxelShape;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

public class VoxelShapeUtil {
    private static final RealMatrix matrixRotEast = new Array2DRowRealMatrix(new double[][]{
            {0, 0, 1, 0},
            {0, 1, 0, 0},
            {-1, 0, 0, 16}
    });
    private static final RealMatrix matrixRotSouth = new Array2DRowRealMatrix(new double[][]{
            {-1, 0, 0, 16},
            {0, 1, 0, 0},
            {0, 0, -1, 16}
    });
    private static final RealMatrix matrixRotWest = new Array2DRowRealMatrix(new double[][]{
            {0, 0, -1, 16},
            {0, 1, 0, 0},
            {1, 0, 0, 0}
    });


    public static VoxelShape rotate(RealMatrix matrix, VoxelShape shape) {
        AxisAlignedBB boundingBox = shape.getBoundingBox();
        RealVector beginPoint = new ArrayRealVector(new double[]{boundingBox.minX, boundingBox.minY, boundingBox.minZ, 1});
        RealVector lastPoint = new ArrayRealVector(new double[]{boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ, 1});
        RealVector beginPointTransitioned = matrix.operate(beginPoint);
        RealVector lastPointTransitioned = matrix.operate(lastPoint);
        return Block.makeCuboidShape(beginPointTransitioned.getEntry(0), beginPointTransitioned.getEntry(1), beginPoint.getEntry(2), lastPointTransitioned.getEntry(0), lastPointTransitioned.getEntry(1), lastPoint.getEntry(2));
    }

    public static VoxelShape rotateDirection(VoxelShape shape, Direction direction) {
        if (direction != Direction.UP && direction != Direction.DOWN) {
            switch (direction) {
                case EAST:
                    return rotate(matrixRotEast, shape);
                case WEST:
                    return rotate(matrixRotWest, shape);
                case SOUTH:
                    return rotate(matrixRotSouth, shape);
                default:
                    return shape;
            }
        }
        return shape;
    }
}

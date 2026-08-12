package net.gobbob.mobends.util;

import org.lwjgl.util.vector.Vector3f;

public class GUtil {
    public static float max(float argNum, float argMax) {
        if (argNum > argMax) {
            return argMax;
        }
        return argNum;
    }

    public static Vector3f max(Vector3f argNum, float argMax) {
        Vector3f result = argNum;
        if (argNum.x > argMax) {
            result.x = argMax;
        }
        if (argNum.y > argMax) {
            result.y = argMax;
        }
        if (argNum.z > argMax) {
            result.z = argMax;
        }
        return result;
    }

    public static float min(float argNum, float argMax) {
        if (argNum < argMax) {
            return argMax;
        }
        return argNum;
    }

    public static Vector3f translate(Vector3f num, Vector3f move) {
        num.x += move.x;
        num.y += move.y;
        num.z += move.z;
        return num;
    }

    public static Vector3f scale(Vector3f num, Vector3f move) {
        num.x *= move.x;
        num.y *= move.y;
        num.z *= move.z;
        return num;
    }

    public static Vector3f rotateX(Vector3f num, float rotation) {
        Vector3f y = new Vector3f();
        Vector3f z = new Vector3f();
        y.y = (float)Math.cos((double)((180.0f + rotation) / 180.0f) * Math.PI);
        y.z = (float)Math.sin((double)((180.0f + rotation) / 180.0f) * Math.PI);
        y.normalise();
        y.y *= -num.y;
        y.z *= num.y;
        z.y = (float)Math.sin((double)((180.0f + rotation) / 180.0f) * Math.PI);
        z.z = (float)Math.cos((double)((180.0f + rotation) / 180.0f) * Math.PI);
        z.normalise();
        z.y *= -num.z;
        z.z *= -num.z;
        num = new Vector3f(num.x, y.y + z.y, y.z + z.z);
        return num;
    }

    public static Vector3f rotateY(Vector3f num, float rotation) {
        Vector3f x = new Vector3f();
        Vector3f z = new Vector3f();
        x.x = (float)Math.cos((double)(-rotation / 180.0f) * Math.PI);
        x.z = (float)Math.sin((double)(-rotation / 180.0f) * Math.PI);
        x.normalise();
        x.x *= -num.x;
        x.z *= num.x;
        z.x = (float)Math.sin((double)(-rotation / 180.0f) * Math.PI);
        z.z = (float)Math.cos((double)(-rotation / 180.0f) * Math.PI);
        z.normalise();
        z.x *= num.z;
        z.z *= num.z;
        num = new Vector3f(x.x + z.x, num.y, x.z + z.z);
        return num;
    }

    public static Vector3f rotateZ(Vector3f num, float rotation) {
        Vector3f x = new Vector3f();
        Vector3f y = new Vector3f();
        x.x = (float)Math.sin((double)((rotation - 90.0f) / 180.0f) * Math.PI);
        x.y = (float)Math.cos((double)((rotation - 90.0f) / 180.0f) * Math.PI);
        x.normalise();
        x.x *= -num.x;
        x.y *= num.x;
        y.x = (float)Math.cos((double)((rotation - 90.0f) / 180.0f) * Math.PI);
        y.y = (float)Math.sin((double)((rotation - 90.0f) / 180.0f) * Math.PI);
        y.normalise();
        y.x *= -num.y;
        y.y *= -num.y;
        num = new Vector3f(y.x + x.x, y.y + x.y, num.z);
        return num;
    }

    public static Vector3f[] translate(Vector3f[] nums, Vector3f move) {
        for (int i = 0; i < nums.length; ++i) {
            nums[i] = GUtil.translate(nums[i], move);
        }
        return nums;
    }

    public static Vector3f[] scale(Vector3f[] nums, Vector3f move) {
        for (int i = 0; i < nums.length; ++i) {
            nums[i] = GUtil.scale(nums[i], move);
        }
        return nums;
    }

    public static Vector3f[] rotateX(Vector3f[] nums, float move) {
        for (int i = 0; i < nums.length; ++i) {
            nums[i] = GUtil.rotateX(nums[i], move);
        }
        return nums;
    }

    public static Vector3f[] rotateY(Vector3f[] nums, float move) {
        for (int i = 0; i < nums.length; ++i) {
            nums[i] = GUtil.rotateY(nums[i], move);
        }
        return nums;
    }

    public static Vector3f[] rotateZ(Vector3f[] nums, float move) {
        for (int i = 0; i < nums.length; ++i) {
            nums[i] = GUtil.rotateZ(nums[i], move);
        }
        return nums;
    }
}


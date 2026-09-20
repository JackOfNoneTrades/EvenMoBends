package net.gobbob.mobends.client.renderer;

import net.gobbob.mobends.client.model.ModelRendererBends;
import net.gobbob.mobends.client.model.entity.ModelBendsPlayer;
import net.minecraft.client.model.ModelRenderer;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Quaternion;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

/** Clearance layered over the original swim stroke and held-item attachment. */
public final class SwimmingArmClearance {
    private static final float HEAD_HALF_SIZE = 4.75f;
    private static final float ITEM_CLEARANCE = 4.95f;
    private static final float ITEM_RETURN_SPEED = 0.55f;
    private static final float ITEM_TURN_PER_TICK = 12;
    private static final float DOWNWARD_ARM_OPENING = 18;
    private static final Vector3f[] HEAD_AXES = {
        new Vector3f(1, 0, 0), new Vector3f(0, 1, 0), new Vector3f(0, 0, 1)
    };
    // Convex envelope of the vanilla sword, pickaxe, axe, shovel and hoe icons.
    // Unlike the full icon square, this excludes the large transparent corners.
    private static final float[] TOOL_OUTLINE = {
        0, 13, 1, 5, 2, 4, 6, 2, 13, 0, 16, 0, 16, 3, 14, 10, 9, 15, 3, 16, 0, 16
    };
    private static final float[] FIST_OUTLINE = {0, 3, 4, 3, 4, 6, 0, 6};
    private static final Matrix4f TOOL = SwimmingItemTransform.vanillaTool();

    private SwimmingArmClearance() {}

    /** Solve a settled pose, used when there is no preceding animation frame. */
    public static void update(ModelBendsPlayer model) {
        update(model, Float.POSITIVE_INFINITY);
    }

    public static void update(ModelBendsPlayer model, float ticksPerFrame) {
        Vector4f previousRight = new Vector4f(model.swimmingRightArmAdjustment);
        Vector4f previousLeft = new Vector4f(model.swimmingLeftArmAdjustment);
        Vector3f previousElbows = new Vector3f(model.swimmingElbowAdjustment);
        Vector4f previousRightItem = new Vector4f(model.swimmingRightItemAdjustment);
        Vector4f previousLeftItem = new Vector4f(model.swimmingLeftItemAdjustment);
        model.swimmingRightItemAdjustment.set(0, 0, 0, 0);
        model.swimmingLeftItemAdjustment.set(0, 0, 0, 0);
        model.swimmingRightArmAdjustment.set(0, 0, 0, 0);
        model.swimmingLeftArmAdjustment.set(0, 0, 0, 0);
        model.swimmingElbowAdjustment.set(0, 0, 0);
        if (model.aimedBow) return;
        Matrix4f headInverse = Matrix4f.invert(part(model.bipedHead), null);
        adjust(model, false, headInverse, previousRight, previousElbows.x);
        adjust(model, true, headInverse, previousLeft, previousElbows.y);
        makeGripRoom(model, false, headInverse);
        makeGripRoom(model, true, headInverse);
        adjustItem(model, false, headInverse, previousRightItem, ticksPerFrame);
        adjustItem(model, true, headInverse, previousLeftItem, ticksPerFrame);
        // Fade the complete correction back to ordinary rendering when movement stops.
        float rightWeight = Math.max(0, Math.min(1, model.swimmingItemPose.getX()));
        float leftWeight = Math.max(0, Math.min(1, model.swimmingItemPose.getY()));
        model.swimmingRightArmAdjustment.w *= rightWeight;
        model.swimmingLeftArmAdjustment.w *= leftWeight;
        model.swimmingElbowAdjustment.x *= rightWeight;
        model.swimmingElbowAdjustment.y *= leftWeight;
        model.swimmingRightItemAdjustment.w *= rightWeight;
        model.swimmingLeftItemAdjustment.w *= leftWeight;
    }

    private static void adjust(ModelBendsPlayer model, boolean left, Matrix4f headInverse,
        Vector4f previous, float previousElbow) {
        int held = left ? model.heldItemLeft : model.heldItemRight;
        float weight = left ? model.swimmingItemPose.getY() : model.swimmingItemPose.getX();
        if (held == 0 || held == 3 || weight <= 0.001f) return;

        ModelRenderer upper = left ? model.bipedLeftArm : model.bipedRightArm;
        ModelRenderer lower = left ? model.bipedLeftForeArm : model.bipedRightForeArm;
        Vector4f adjustment = left ? model.swimmingLeftArmAdjustment : model.swimmingRightArmAdjustment;
        Matrix4f upperRotation = new Matrix4f();
        SwimmingItemTransform.rotatePart(upperRotation, upper);
        Matrix4f shoulder = new Matrix4f(headInverse);
        shoulder.translate(new Vector3f(upper.rotationPointX, upper.rotationPointY, upper.rotationPointZ));
        if (clear(model, left, shoulder, Matrix4f.mul(upperRotation, part(lower), null), lower, adjustment)) return;

        // Most frames only need a tiny change along the already established path.
        // Keep that path and avoid repeating the wider search for every player/frame.
        if (previous.w > 0) {
            if (left) model.swimmingElbowAdjustment.y = previousElbow;
            else model.swimmingElbowAdjustment.x = previousElbow;
            adjustment.set(previous);
            float angle = findClearAngle(model, left, shoulder,
                forearm(upperRotation, lower, previousElbow), lower, adjustment, 90);
            if (Math.abs(angle - previous.w) < 2) {
                adjustment.w = angle;
                return;
            }
        }

        Vector4f headCentre = Matrix4f.transform(Matrix4f.invert(headInverse, null),
            new Vector4f(0, -4, 0, 1), null);
        Vector4f best = new Vector4f();
        float bestCost = Float.POSITIVE_INFINITY;
        float bestElbow = 0;
        // A little elbow opening avoids having to swing an entire tightly folded arm
        // around the head. The original elbow and shoulder cycles remain underneath.
        for (float elbow = 0; elbow <= 30; elbow += 5) {
            if (elbow >= bestCost) break;
            if (left) model.swimmingElbowAdjustment.y = elbow;
            else model.swimmingElbowAdjustment.x = elbow;
            Matrix4f forearm = forearm(upperRotation, lower, elbow);
            Matrix4f attachment = new Matrix4f(forearm);
            attachment.translate(new Vector3f(-lower.rotationPointX, -lower.rotationPointY, -lower.rotationPointZ));
            Vector4f palm = Matrix4f.transform(attachment, SwimmingItemTransform.palm(model, left), null);
            Vector3f away = new Vector3f(palm.x + upper.rotationPointX - headCentre.x + (left ? 2 : -2),
                palm.y + upper.rotationPointY - headCentre.y, palm.z + upper.rotationPointZ - headCentre.z);
            Vector3f axis = Vector3f.cross(new Vector3f(palm.x, palm.y, palm.z), away, null);
            if (axis.lengthSquared() < 0.0001f) continue;
            axis.normalise();
            Vector3f tangent = Vector3f.cross(new Vector3f(palm.x, palm.y, palm.z), axis, null);
            tangent.normalise();
            for (int direction = previous.w > 0 ? -1 : 0; direction < 12; direction++) {
                float turn = (float)(direction * Math.PI / 6);
                adjustment.set(axis.x * (float)Math.cos(turn) + tangent.x * (float)Math.sin(turn),
                    axis.y * (float)Math.cos(turn) + tangent.y * (float)Math.sin(turn),
                    axis.z * (float)Math.cos(turn) + tangent.z * (float)Math.sin(turn), 0);
                if (direction == -1) adjustment.set(previous.x, previous.y, previous.z, 0);
                float angle = findClearAngle(model, left, shoulder, forearm, lower, adjustment,
                    Math.min(90, bestCost - elbow));
                adjustment.w = angle;
                float cost = cost(adjustment, elbow, previous, previousElbow);
                if (cost < bestCost) {
                    bestCost = cost;
                    best.set(adjustment.x, adjustment.y, adjustment.z, angle);
                    bestElbow = elbow;
                }
            }
        }
        if (left) model.swimmingElbowAdjustment.y = bestElbow;
        else model.swimmingElbowAdjustment.x = bestElbow;
        Matrix4f forearm = forearm(upperRotation, lower, bestElbow);
        Matrix4f attachment = new Matrix4f(forearm);
        attachment.translate(new Vector3f(-lower.rotationPointX, -lower.rotationPointY, -lower.rotationPointZ));
        Vector4f palm = Matrix4f.transform(attachment, SwimmingItemTransform.palm(model, left), null);
        // Refine between the coarse search directions so neighbouring frames do not
        // jump between discrete arm paths.
        for (float spread = 15; spread >= 1.875f && best.w > 0; spread *= 0.5f) {
            Vector3f axis = new Vector3f(best.x, best.y, best.z);
            Vector3f tangent = Vector3f.cross(new Vector3f(palm.x, palm.y, palm.z), axis, null);
            tangent.normalise();
            for (int sign : new int[] {-1, 1}) {
                float turn = (float)Math.toRadians(spread * sign);
                adjustment.set(axis.x * (float)Math.cos(turn) + tangent.x * (float)Math.sin(turn),
                    axis.y * (float)Math.cos(turn) + tangent.y * (float)Math.sin(turn),
                    axis.z * (float)Math.cos(turn) + tangent.z * (float)Math.sin(turn), 0);
                float angle = findClearAngle(model, left, shoulder, forearm, lower, adjustment,
                    Math.min(90, bestCost - bestElbow));
                adjustment.w = angle;
                float cost = cost(adjustment, bestElbow, previous, previousElbow);
                if (cost < bestCost) {
                    bestCost = cost;
                    best.set(adjustment);
                }
            }
        }
        adjustment.set(best);
    }

    private static float cost(Vector4f adjustment, float elbow, Vector4f previous, float previousElbow) {
        if (!Float.isFinite(adjustment.w)) return Float.POSITIVE_INFINITY;
        float dx = adjustment.x * adjustment.w - previous.x * previous.w;
        float dy = adjustment.y * adjustment.w - previous.y * previous.w;
        float dz = adjustment.z * adjustment.w - previous.z * previous.w;
        // Favour the preceding path when two sides of the head are equally clear.
        // This avoids switching abruptly between equally short solutions.
        return adjustment.w + elbow + 2.0f * ((float)Math.sqrt(dx * dx + dy * dy + dz * dz)
            + Math.abs(elbow - previousElbow));
    }

    private static Matrix4f forearm(Matrix4f upperRotation, ModelRenderer lower, float elbow) {
        Matrix4f matrix = new Matrix4f();
        matrix.translate(new Vector3f(lower.rotationPointX, lower.rotationPointY, lower.rotationPointZ));
        matrix.rotate((float)Math.toRadians(elbow), new Vector3f(1, 0, 0));
        SwimmingItemTransform.rotatePart(matrix, lower);
        return Matrix4f.mul(upperRotation, matrix, matrix);
    }

    private static float findClearAngle(ModelBendsPlayer model, boolean left, Matrix4f shoulder,
        Matrix4f forearm, ModelRenderer lower, Vector4f adjustment, float limit) {
        adjustment.w = 0;
        if (clear(model, left, shoulder, forearm, lower, adjustment)) return 0;
        float blocked = 0;
        for (float angle = 4; angle <= limit + 4; angle += 4) {
            adjustment.w = Math.min(angle, limit);
            if (clear(model, left, shoulder, forearm, lower, adjustment)) {
                float safe = adjustment.w;
                for (int i = 0; i < 7; i++) {
                    adjustment.w = (blocked + safe) * 0.5f;
                    if (clear(model, left, shoulder, forearm, lower, adjustment)) safe = adjustment.w;
                    else blocked = adjustment.w;
                }
                return safe;
            }
            blocked = angle;
            if (angle >= limit) break;
        }
        return Float.POSITIVE_INFINITY;
    }

    private static boolean clear(ModelBendsPlayer model, boolean left, Matrix4f shoulder,
        Matrix4f forearm, ModelRenderer lower, Vector4f adjustment) {
        Matrix4f hand = new Matrix4f(shoulder);
        if (adjustment.w != 0) hand.rotate((float)Math.toRadians(adjustment.w),
            new Vector3f(adjustment.x, adjustment.y, adjustment.z));
        Matrix4f.mul(hand, forearm, hand);
        return !intersectsHead(hand, FIST_OUTLINE, -4, 0, HEAD_HALF_SIZE + 0.5f * lookingDown(model));
    }

    /** Open the stroke slightly while looking down, so clearance does not all come from the grip. */
    private static void makeGripRoom(ModelBendsPlayer model, boolean left, Matrix4f headInverse) {
        int held = left ? model.heldItemLeft : model.heldItemRight;
        float weight = left ? model.swimmingItemPose.getY() : model.swimmingItemPose.getX();
        float down = lookingDown(model);
        if (held == 0 || held == 3 || weight <= 0.001f || down == 0) return;
        ModelRenderer upper = left ? model.bipedLeftArm : model.bipedRightArm;
        ModelRenderer lower = left ? model.bipedLeftForeArm : model.bipedRightForeArm;
        Vector4f arm = left ? model.swimmingLeftArmAdjustment : model.swimmingRightArmAdjustment;
        Matrix4f hand = new Matrix4f();
        if (arm.w != 0) hand.rotate((float)Math.toRadians(arm.w), new Vector3f(arm.x, arm.y, arm.z));
        SwimmingItemTransform.rotatePart(hand, upper);
        Matrix4f.mul(hand, forearm(new Matrix4f(), lower,
            left ? model.swimmingElbowAdjustment.y : model.swimmingElbowAdjustment.x), hand);
        Vector4f palm = Matrix4f.transform(hand, new Vector4f(model.isSlim() ? 1.5f : 2, 4.5f, -2, 1), null);
        Vector3f axis = Vector3f.cross(new Vector3f(palm.x, palm.y, palm.z), new Vector3f(left ? 1 : -1, 0, 0), null);
        if (axis.lengthSquared() < 0.0001f) return;
        axis.normalise();
        Matrix4f shoulder = new Matrix4f(headInverse);
        shoulder.translate(new Vector3f(upper.rotationPointX, upper.rotationPointY, upper.rotationPointZ));
        float openingAngle = DOWNWARD_ARM_OPENING * down;
        // An outward movement can still graze a corner of a turned head. Shorten
        // the opening continuously in those poses before computing the tool tilt.
        Vector4f openingRotation = new Vector4f(axis.x, axis.y, axis.z, openingAngle);
        if (!clear(model, left, shoulder, hand, lower, openingRotation)) {
            float safe = 0;
            float blocked = openingAngle;
            for (int i = 0; i < 8; i++) {
                openingRotation.w = (safe + blocked) * 0.5f;
                if (clear(model, left, shoulder, hand, lower, openingRotation)) safe = openingRotation.w;
                else blocked = openingRotation.w;
            }
            openingAngle = safe;
        }
        Quaternion opening = quaternion(new Vector4f(axis.x, axis.y, axis.z, openingAngle));
        setItemRotation(arm, Quaternion.mul(opening, quaternion(arm), null));
    }

    private static float lookingDown(ModelBendsPlayer model) {
        ModelRendererBends head = (ModelRendererBends)model.bipedHead;
        float relativePitch = head.pre_rotation.getX() + head.rotation.getX();
        // AA pitches the whole player and keeps the head extended relative to the
        // shoulders. Only a head actually lowered towards the hands needs this room.
        return Math.max(0, Math.min(1, (model.headRotationX - 10) / 35))
            * Math.max(0, Math.min(1, (relativePitch + 45) / 30));
    }

    private static void adjustItem(ModelBendsPlayer model, boolean left, Matrix4f headInverse,
        Vector4f previous, float ticksPerFrame) {
        int held = left ? model.heldItemLeft : model.heldItemRight;
        float weight = left ? model.swimmingItemPose.getY() : model.swimmingItemPose.getX();
        if (held == 0 || held == 3 || weight <= 0.001f) return;
        ModelRenderer upper = left ? model.bipedLeftArm : model.bipedRightArm;
        ModelRenderer lower = left ? model.bipedLeftForeArm : model.bipedRightForeArm;
        Vector4f arm = left ? model.swimmingLeftArmAdjustment : model.swimmingRightArmAdjustment;
        Matrix4f hand = new Matrix4f(headInverse);
        hand.translate(new Vector3f(upper.rotationPointX, upper.rotationPointY, upper.rotationPointZ));
        if (arm.w != 0) hand.rotate((float)Math.toRadians(arm.w), new Vector3f(arm.x, arm.y, arm.z));
        SwimmingItemTransform.rotatePart(hand, upper);
        Matrix4f.mul(hand, forearm(new Matrix4f(), lower,
            left ? model.swimmingElbowAdjustment.y : model.swimmingElbowAdjustment.x), hand);
        hand.translate(new Vector3f(-lower.rotationPointX, -lower.rotationPointY, -lower.rotationPointZ));

        if (Float.isFinite(ticksPerFrame) && followOriginalItem(model, left, hand, previous, ticksPerFrame)) return;

        // Begin easing away before contact. If that extra margin cannot fit around
        // a tightly folded fist, the smaller pass still clears the head and hat.
        if (!findItemAdjustment(model, left, hand, previous, ITEM_CLEARANCE)) {
            findItemAdjustment(model, left, hand, previous, HEAD_HALF_SIZE);
        }
        easeItem(model, left, hand, previous, ticksPerFrame);
    }

    private static boolean followOriginalItem(ModelBendsPlayer model, boolean left, Matrix4f hand,
        Vector4f previous, float ticksPerFrame) {
        Vector4f item = left ? model.swimmingLeftItemAdjustment : model.swimmingRightItemAdjustment;
        float elapsed = Math.max(0, ticksPerFrame);
        item.set(0, 0, 0, 0);
        float down = lookingDown(model);
        // Re-evaluate from the natural grip when the head drops. Following only the
        // previous clearance boundary can wind the item past 100 degrees even though
        // a much smaller tilt has become possible on the other side of that boundary.
        if (down > 0 && !projectItem(model, left, hand, ITEM_CLEARANCE)) return false;
        item.w *= down;
        Quaternion start = quaternion(previous);
        Quaternion target = quaternion(item);
        float targetDot = Quaternion.dot(start, target);
        if (targetDot < 0) target.set(-target.x, -target.y, -target.z, -target.w);
        float targetDistance = (float)Math.acos(Math.min(1, Math.abs(targetDot)));
        if (targetDistance > 0.0001f) {
            float amount = Math.min(1 - (float)Math.exp(-ITEM_RETURN_SPEED * elapsed),
                (float)Math.toRadians(ITEM_TURN_PER_TICK * elapsed) / (2 * targetDistance));
            interpolateItem(item, start, target, targetDistance, amount);
        }
        if (!projectItem(model, left, hand, ITEM_CLEARANCE)) return false;
        Quaternion from = quaternion(previous);
        Quaternion to = quaternion(item);
        float dot = Math.abs(Quaternion.dot(from, to));
        float distance = (float)Math.acos(Math.min(1, dot));
        float maximum = (float)Math.toRadians(ITEM_TURN_PER_TICK * elapsed);
        if (distance * 2 > maximum) {
            if (Quaternion.dot(from, to) < 0) to.set(-to.x, -to.y, -to.z, -to.w);
            Vector4f projected = new Vector4f(item);
            interpolateItem(item, from, to, distance, maximum / (2 * distance));
            if (!toolClear(model, left, hand, HEAD_HALF_SIZE) && !projectItem(model, left, hand, HEAD_HALF_SIZE)) {
                item.set(projected);
            }
        }
        return true;
    }

    private static boolean projectItem(ModelBendsPlayer model, boolean left, Matrix4f hand, float padding) {
        Vector4f item = left ? model.swimmingLeftItemAdjustment : model.swimmingRightItemAdjustment;
        // Return towards the ordinary grip every frame, then slide along the head's
        // clearance boundary. A continuous projection avoids discrete escape steps
        // and does not lock the tool in an old avoidance pose.
        for (int iteration = 0; iteration < 12; iteration++) {
            float gap = toolSeparation(model, left, hand, padding);
            if (gap >= 0) return true;
            Quaternion base = quaternion(item);
            Vector3f gradient = new Vector3f();
            float probe = 0.25f;
            for (int axis = 0; axis < 3; axis++) {
                setItemRotation(item, Quaternion.mul(turn(axis, probe), base, null));
                float positive = toolSeparation(model, left, hand, padding);
                setItemRotation(item, Quaternion.mul(turn(axis, -probe), base, null));
                float negative = toolSeparation(model, left, hand, padding);
                float slope = (positive - negative) / (2 * probe);
                if (axis == 0) gradient.x = slope;
                else if (axis == 1) gradient.y = slope;
                else gradient.z = slope;
            }
            float length = gradient.length();
            if (length < 0.0001f) return false;
            gradient.scale(1 / length);
            float angle = Math.min(8, (0.005f - gap) / length);
            float half = (float)Math.toRadians(angle) * 0.5f;
            float sin = (float)Math.sin(half);
            Quaternion step = new Quaternion(gradient.x * sin, gradient.y * sin, gradient.z * sin,
                (float)Math.cos(half));
            setItemRotation(item, Quaternion.mul(step, base, null));
        }
        return toolClear(model, left, hand, padding);
    }

    private static Quaternion turn(int axis, float angle) {
        float half = (float)Math.toRadians(angle) * 0.5f;
        float sin = (float)Math.sin(half);
        return new Quaternion(axis == 0 ? sin : 0, axis == 1 ? sin : 0, axis == 2 ? sin : 0,
            (float)Math.cos(half));
    }

    private static void easeItem(ModelBendsPlayer model, boolean left, Matrix4f hand,
        Vector4f previous, float ticksPerFrame) {
        if (!Float.isFinite(ticksPerFrame)) return;
        Vector4f item = left ? model.swimmingLeftItemAdjustment : model.swimmingRightItemAdjustment;
        Vector4f target = new Vector4f(item);
        Quaternion from = quaternion(previous);
        Quaternion to = quaternion(target);
        float dot = Quaternion.dot(from, to);
        if (dot < 0) {
            to.set(-to.x, -to.y, -to.z, -to.w);
            dot = -dot;
        }
        float distance = (float)Math.acos(Math.min(1, dot));
        if (distance < 0.0001f) return;
        float elapsed = Math.max(0, ticksPerFrame);
        // Shortest-path rotation: ease out of the clearance pose instead of dropping
        // it to identity, and cap sudden changes of the selected avoidance direction.
        float amount = Math.min(1 - (float)Math.exp(-ITEM_RETURN_SPEED * elapsed),
            (float)Math.toRadians(ITEM_TURN_PER_TICK * elapsed) / (2 * distance));
        interpolateItem(item, from, to, distance, amount);
        if (toolClear(model, left, hand, HEAD_HALF_SIZE)) return;
        if (projectItem(model, left, hand, HEAD_HALF_SIZE)) return;

        // Moving towards the head may require a faster correction. Use the earliest
        // clear point on the same arc; releasing the pose remains fully eased.
        float blocked = amount;
        float clear = 1;
        for (int i = 0; i < 8; i++) {
            float step = (blocked + clear) * 0.5f;
            interpolateItem(item, from, to, distance, step);
            if (toolClear(model, left, hand, HEAD_HALF_SIZE)) clear = step;
            else blocked = step;
        }
        if (clear == 1) item.set(target);
        else interpolateItem(item, from, to, distance, clear);
    }

    private static Quaternion quaternion(Vector4f rotation) {
        float halfAngle = (float)Math.toRadians(rotation.w) * 0.5f;
        float sin = (float)Math.sin(halfAngle);
        return new Quaternion(rotation.x * sin, rotation.y * sin, rotation.z * sin,
            (float)Math.cos(halfAngle));
    }

    private static void interpolateItem(Vector4f rotation, Quaternion from, Quaternion to,
        float distance, float amount) {
        float denominator = (float)Math.sin(distance);
        float a = (float)Math.sin((1 - amount) * distance) / denominator;
        float b = (float)Math.sin(amount * distance) / denominator;
        Quaternion q = new Quaternion(from.x * a + to.x * b, from.y * a + to.y * b,
            from.z * a + to.z * b, from.w * a + to.w * b);
        setItemRotation(rotation, q);
    }

    private static void setItemRotation(Vector4f rotation, Quaternion q) {
        q.normalise();
        if (q.w < 0) q.set(-q.x, -q.y, -q.z, -q.w);
        float axisLength = (float)Math.sqrt(q.x * q.x + q.y * q.y + q.z * q.z);
        if (axisLength < 0.00001f) rotation.set(0, 0, 0, 0);
        else rotation.set(q.x / axisLength, q.y / axisLength, q.z / axisLength,
            (float)Math.toDegrees(2 * Math.atan2(axisLength, q.w)));
    }

    private static boolean findItemAdjustment(ModelBendsPlayer model, boolean left, Matrix4f hand,
        Vector4f previous, float padding) {
        Vector4f item = left ? model.swimmingLeftItemAdjustment : model.swimmingRightItemAdjustment;
        item.set(0, 0, 0, 0);
        if (toolClear(model, left, hand, padding)) return true;
        if (previous.w > 0) {
            item.set(previous);
            float angle = findItemAngle(model, left, hand, padding, 110);
            if (Math.abs(angle - previous.w) < 2) {
                item.w = angle;
                return true;
            }
        }
        Vector3f grip = SwimmingItemTransform.grip(left, 1);
        Vector4f head = Matrix4f.transform(Matrix4f.invert(hand, null), new Vector4f(0, -4, 0, 1), null);
        Vector3f blade = SwimmingItemTransform.blade(left);
        Vector3f axis = Vector3f.cross(blade, new Vector3f(grip.x - head.x, grip.y - head.y, grip.z - head.z), null);
        if (axis.lengthSquared() < 0.0001f) axis = Vector3f.cross(blade, new Vector3f(1, 0, 0), null);
        axis.normalise();
        Vector3f tangent = Vector3f.cross(blade, axis, null);
        tangent.normalise();
        Vector4f best = new Vector4f();
        float bestCost = Float.POSITIVE_INFINITY;
        for (int direction = previous.w > 0 ? -9 : 0; direction < 12; direction++) {
            float turn = (float)(direction * Math.PI / 6);
            item.set(axis.x * (float)Math.cos(turn) + tangent.x * (float)Math.sin(turn),
                axis.y * (float)Math.cos(turn) + tangent.y * (float)Math.sin(turn),
                axis.z * (float)Math.cos(turn) + tangent.z * (float)Math.sin(turn), 0);
            if (direction < 0) {
                Vector3f oldAxis = new Vector3f(previous.x, previous.y, previous.z);
                Vector3f oldTangent = Vector3f.cross(blade, oldAxis, null).normalise(null);
                int offset = -direction - 1;
                float degrees = offset == 0 ? 0 : (3 << ((offset - 1) / 2)) * (offset % 2 == 0 ? -1 : 1);
                turn = (float)Math.toRadians(degrees);
                item.set(oldAxis.x * (float)Math.cos(turn) + oldTangent.x * (float)Math.sin(turn),
                    oldAxis.y * (float)Math.cos(turn) + oldTangent.y * (float)Math.sin(turn),
                    oldAxis.z * (float)Math.cos(turn) + oldTangent.z * (float)Math.sin(turn), 0);
            }
            item.w = findItemAngle(model, left, hand, padding, Math.min(110, bestCost));
            float cost = cost(item, 0, previous, 0);
            if (cost < bestCost) {
                bestCost = cost;
                best.set(item);
            }
        }
        item.set(best);
        return Float.isFinite(bestCost);
    }

    private static float findItemAngle(ModelBendsPlayer model, boolean left, Matrix4f hand,
        float padding, float limit) {
        Vector4f item = left ? model.swimmingLeftItemAdjustment : model.swimmingRightItemAdjustment;
        float blocked = 0;
        for (float angle = 4; angle <= limit + 4; angle += 4) {
            item.w = Math.min(angle, limit);
            if (toolClear(model, left, hand, padding)) {
                float safe = item.w;
                for (int i = 0; i < 7; i++) {
                    item.w = (blocked + safe) * 0.5f;
                    if (toolClear(model, left, hand, padding)) safe = item.w;
                    else blocked = item.w;
                }
                return safe;
            }
            blocked = angle;
            if (angle >= limit) break;
        }
        return Float.POSITIVE_INFINITY;
    }

    private static Matrix4f toolMatrix(ModelBendsPlayer model, boolean left, Matrix4f hand) {
        Matrix4f tool = Matrix4f.mul(hand, SwimmingItemTransform.matrix(model, left, 1), null);
        if (left) tool.scale(new Vector3f(-1, 1, 1));
        tool.translate(new Vector3f(-1, 7, 1));
        Matrix4f.mul(tool, TOOL, tool);
        return tool;
    }

    private static boolean toolClear(ModelBendsPlayer model, boolean left, Matrix4f hand, float padding) {
        return !intersectsHead(toolMatrix(model, left, hand), TOOL_OUTLINE, -1, 0, padding);
    }

    private static float toolSeparation(ModelBendsPlayer model, boolean left, Matrix4f hand, float padding) {
        return headSeparation(toolMatrix(model, left, hand), TOOL_OUTLINE, -1, 0, padding, false);
    }

    private static Matrix4f part(ModelRenderer part) {
        Matrix4f matrix = new Matrix4f();
        matrix.translate(new Vector3f(part.rotationPointX, part.rotationPointY, part.rotationPointZ));
        SwimmingItemTransform.rotatePart(matrix, part);
        return matrix;
    }

    /** Separating-axis test for an extruded icon/hand against the head and hat. */
    private static boolean intersectsHead(Matrix4f matrix, float[] outline, float back, float front, float padding) {
        return headSeparation(matrix, outline, back, front, padding, true) <= 0;
    }

    /** Signed separation in model pixels; positive means clear of the head. */
    private static float headSeparation(Matrix4f matrix, float[] outline, float back, float front,
        float padding, boolean stopWhenClear) {
        int count = outline.length / 2;
        Vector3f[] vertices = new Vector3f[count * 2];
        for (int i = 0; i < count; i++) {
            for (int layer = 0; layer < 2; layer++) {
                Vector4f p = Matrix4f.transform(matrix,
                    new Vector4f(outline[i * 2], outline[i * 2 + 1], layer == 0 ? back : front, 1), null);
                vertices[i + layer * count] = new Vector3f(p.x, p.y + 4, p.z);
            }
        }
        float gap = Float.NEGATIVE_INFINITY;
        for (Vector3f axis : HEAD_AXES) {
            gap = Math.max(gap, separation(vertices, axis, padding));
            if (stopWhenClear && gap > 0) return gap;
        }
        Vector3f depth = Vector3f.sub(vertices[count], vertices[0], null);
        gap = Math.max(gap, separation(vertices, depth, padding));
        if (stopWhenClear && gap > 0) return gap;
        for (Vector3f axis : HEAD_AXES) {
            gap = Math.max(gap, separation(vertices, Vector3f.cross(depth, axis, null), padding));
            if (stopWhenClear && gap > 0) return gap;
        }
        for (int i = 0; i < count; i++) {
            Vector3f edge = Vector3f.sub(vertices[(i + 1) % count], vertices[i], null);
            gap = Math.max(gap, separation(vertices, Vector3f.cross(edge, depth, null), padding));
            if (stopWhenClear && gap > 0) return gap;
            for (Vector3f axis : HEAD_AXES) {
                gap = Math.max(gap, separation(vertices, Vector3f.cross(edge, axis, null), padding));
                if (stopWhenClear && gap > 0) return gap;
            }
        }
        return gap;
    }

    private static float separation(Vector3f[] vertices, Vector3f axis, float padding) {
        if (axis.lengthSquared() < 0.000001f) return Float.NEGATIVE_INFINITY;
        float radius = padding * (Math.abs(axis.x) + Math.abs(axis.y) + Math.abs(axis.z));
        float min = Float.POSITIVE_INFINITY;
        float max = Float.NEGATIVE_INFINITY;
        for (Vector3f vertex : vertices) {
            float dot = Vector3f.dot(vertex, axis);
            min = Math.min(min, dot);
            max = Math.max(max, dot);
        }
        return Math.max(min - radius, -radius - max) / axis.length();
    }
}

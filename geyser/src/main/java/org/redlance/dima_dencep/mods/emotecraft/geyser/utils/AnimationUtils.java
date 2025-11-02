package org.redlance.dima_dencep.mods.emotecraft.geyser.utils;

import com.zigythebird.playeranimcore.animation.Animation;
import com.zigythebird.playeranimcore.animation.keyframe.BoneAnimation;
import com.zigythebird.playeranimcore.math.Vec3f;

import java.util.Map;

public class AnimationUtils {
    public static Animation javaToGeyserAnimation(Animation animation) {
        Map<String, BoneAnimation> boneAnimations = animation.boneAnimations();
        if (boneAnimations.containsKey("body")) {
            boneAnimations.put("body_java", boneAnimations.get("body"));
            boneAnimations.remove("body");
            animation.parents().put("body", "body_java");
            animation.bones().put("body_java", new Vec3f(0, 12, 0));
        }
        if (boneAnimations.containsKey("torso")) {
            animation.parents().put("right_arm", "negated_torso_for_bedrock");
            animation.parents().put("left_arm", "negated_torso_for_bedrock");
            animation.parents().put("head", "negated_torso_for_bedrock");
            animation.bones().put("negated_torso_for_bedrock", new Vec3f(0, 24, 0));
        }
        return animation;
    }
}

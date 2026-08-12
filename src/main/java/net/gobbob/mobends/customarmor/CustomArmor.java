package net.gobbob.mobends.customarmor;

import java.util.ArrayList;
import java.util.List;
import net.gobbob.mobends.client.model.ModelCustomArmor;
import net.gobbob.mobends.customarmor.ArmorConverter;
import net.minecraft.client.model.ModelBiped;

public class CustomArmor {
    public static List<CustomArmor> armorList = new ArrayList<CustomArmor>();
    public ModelCustomArmor armorModel;
    public String texturePath;

    public static CustomArmor get(ModelBiped argOriginal, String argTexture, float argModelBuffnes) {
        for (int i = 0; i < armorList.size(); ++i) {
            if (!CustomArmor.armorList.get((int)i).texturePath.equalsIgnoreCase(argTexture)) continue;
            return armorList.get(i);
        }
        CustomArmor newArmor = ArmorConverter.convert(argModelBuffnes, argOriginal, argTexture);
        armorList.add(newArmor);
        return newArmor;
    }

    public CustomArmor() {
        this.armorModel = null;
        this.texturePath = null;
    }

    public CustomArmor(ModelCustomArmor argArmor, String argTexture) {
        this.armorModel = argArmor;
        this.texturePath = argTexture;
    }
}


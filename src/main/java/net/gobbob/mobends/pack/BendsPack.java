package net.gobbob.mobends.pack;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import net.gobbob.mobends.client.model.ModelRendererBends;
import net.gobbob.mobends.client.model.entity.ModelBendsPlayer;
import net.gobbob.mobends.client.model.entity.ModelBendsSpider;
import net.gobbob.mobends.client.model.entity.ModelBendsZombie;
import net.gobbob.mobends.pack.BendsAction;
import net.gobbob.mobends.pack.BendsTarget;
import net.gobbob.mobends.util.BendsLogger;
import net.gobbob.mobends.util.EnumAxis;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.config.Configuration;

public class BendsPack {
    private static final class Operation {
        private String operator = "";
        private String num = "";
        private String globalVar;
    }

    public static File bendsPacksDir;
    public static List<BendsPack> bendsPacks;
    public static int currentPack;
    public String filename;
    public String displayName;
    public String author;
    public String description;
    public static List<BendsTarget> targets;

    public void readBasicInfo(File file) throws IOException {
        this.filename = file.getName();
        BufferedReader reader = new BufferedReader(new FileReader(file.getAbsolutePath()));
        String line = reader.readLine();
        while (line != null) {
            String data;
            BendsLogger.log(line, BendsLogger.DEBUG);
            if (line.startsWith("name:")) {
                data = line;
                this.displayName = BendsPack.formatStringData("name:", data);
            } else if (line.startsWith("author:")) {
                data = line;
                this.author = BendsPack.formatStringData("author:", data);
            } else if (line.startsWith("description:")) {
                data = line;
                this.description = BendsPack.formatStringData("description:", data);
            }
            line = reader.readLine();
        }
        reader.close();
    }

    public void apply() throws IOException {
        if (this.filename == null) {
            targets.clear();
            return;
        }
        File file = new File(bendsPacksDir, this.filename);
        BufferedReader reader = new BufferedReader(new FileReader(file.getAbsolutePath()));
        targets.clear();
        boolean override = false;
        String anim = "";
        String line = reader.readLine();
        while (line != null) {
            String data;
            if (line.startsWith("target")) {
                data = line;
                data = BendsPack.formatStringData("target", data);
                targets.add(new BendsTarget(data.toLowerCase()));
                override = false;
            } else if (line.contains("anim")) {
                data = line;
                anim = BendsPack.formatStringData("anim", data);
            } else if (line.contains("override: true")) {
                override = true;
            } else if (line.contains("override: false")) {
                override = false;
            } else if (line.contains("@") && targets.size() > 0) {
                BendsPack.targets.get((int)(BendsPack.targets.size() - 1)).actions.add(BendsPack.getActionFromLine(anim, line));
            }
            line = reader.readLine();
        }
        reader.close();
        for (int i = 0; i < targets.size(); ++i) {
            System.out.println("Target: " + BendsPack.targets.get((int)i).mob);
            for (int a = 0; a < BendsPack.targets.get((int)i).actions.size(); ++a) {
                System.out.println("    Action: " + BendsPack.targets.get((int)i).actions.get((int)a).anim + ", " + BendsPack.targets.get((int)i).actions.get((int)a).model + ", " + BendsPack.targets.get((int)i).actions.get((int)a).prop.name() + "-" + (BendsPack.targets.get((int)i).actions.get((int)a).axis != null ? BendsPack.targets.get((int)i).actions.get((int)a).axis.name() : "null") + (BendsPack.targets.get((int)i).actions.get((int)a).mod != null ? BendsPack.targets.get((int)i).actions.get((int)a).mod.name() : "null"));
                for (int c = 0; c < BendsPack.targets.get((int)i).actions.get((int)a).calculations.size(); ++c) {
                    BendsAction.Calculation calc = BendsPack.targets.get((int)i).actions.get((int)a).calculations.get(c);
                    System.out.println("        Calc: " + calc.operator.name() + ", " + (calc.globalVar != null ? calc.globalVar : Float.valueOf(calc.number)) + ", ");
                }
            }
        }
    }

    public static void preInit(Configuration config) {
        bendsPacksDir = new File(Minecraft.getMinecraft().mcDataDir, "bendspacks");
        bendsPacksDir.mkdir();
        currentPack = config.get("General", "Current Pack", 0).getInt();
        try {
            BendsPack.initPacks();
            if (BendsPack.getCurrentPack() != null) {
                BendsPack.getCurrentPack().apply();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void initPacks() throws IOException {
        File[] files = bendsPacksDir.listFiles();
        bendsPacks.clear();
        bendsPacks.add(BendsPack.getDefaultPack());
        for (File file : files) {
            if (!file.getAbsolutePath().endsWith(".bends")) continue;
            BendsLogger.log(file.getAbsolutePath(), BendsLogger.DEBUG);
            BendsPack pack = new BendsPack();
            pack.readBasicInfo(file);
            if (!(pack.filename != null & pack.displayName != null)) continue;
            bendsPacks.add(pack);
        }
        if (currentPack > bendsPacks.size() - 1) {
            currentPack = bendsPacks.size() - 1;
        }
    }

    public static BendsPack getDefaultPack() {
        BendsPack defaultPack = new BendsPack();
        defaultPack.filename = null;
        defaultPack.displayName = "Default";
        defaultPack.author = "GoblinBob";
        defaultPack.description = "The default bends-pack suggested and made by GoblinBob, the creator of Mo' Bends.";
        return defaultPack;
    }

    public static String formatStringData(String header, String data) {
        if ((data = data.replaceFirst(header, "")).contains("\"")) {
            data = data.replaceAll("\"", "");
        }
        if (data.contains("{")) {
            data = data.replace("{", "");
        }
        data = data.trim();
        return data;
    }

    public static BendsPack getCurrentPack() {
        if (currentPack > bendsPacks.size() - 1) {
            currentPack = bendsPacks.size() - 1;
        }
        return bendsPacks.get(currentPack);
    }

    public static BendsAction getActionFromLine(String anim, String line) {
        int i;
        BendsAction action = new BendsAction();
        action.anim = anim;
        action.model = "";
        ArrayList<Operation> calcs = new ArrayList<Operation>();
        calcs.add(new Operation());
        int calc = 0;
        String smooth = "";
        int stage = 0;
        for (i = 0; i < line.length(); ++i) {
            if (stage == 0) {
                if (line.charAt(i) != '@') continue;
                stage = 1;
                continue;
            }
            if (stage == 1) {
                if (line.charAt(i) == ':') {
                    ++stage;
                    continue;
                }
                action.model = action.model + line.charAt(i);
                continue;
            }
            if (stage == 2) {
                if (line.charAt(i) != ' ') continue;
                ++stage;
                continue;
            }
            if (stage == 3) {
                if (line.charAt(i) == ' ') {
                    ++stage;
                    continue;
                }
                ((Operation)calcs.get((int)calc)).operator = ((Operation)calcs.get((int)calc)).operator + line.charAt(i);
                continue;
            }
            if (stage == 4) {
                if (line.charAt(i) == ' ') {
                    ++stage;
                    continue;
                }
                if (line.charAt(i) == '+' | line.charAt(i) == '-' | line.charAt(i) == '=' | line.charAt(i) == '*' | line.charAt(i) == '/') {
                    if (line.charAt(i + 1) == '=') {
                        calcs.add(new Operation());
                        ((Operation)calcs.get((int)(++calc))).operator = line.charAt(i) + "=";
                        ++i;
                        continue;
                    }
                    ((Operation)calcs.get((int)calc)).num = ((Operation)calcs.get((int)calc)).num + line.charAt(i);
                    continue;
                }
                ((Operation)calcs.get((int)calc)).num = ((Operation)calcs.get((int)calc)).num + line.charAt(i);
                continue;
            }
            if (stage != 5) continue;
            if (line.charAt(i) == ' ') {
                ++stage;
                continue;
            }
            smooth = smooth + (line.charAt(i) == '#' ? "" : Character.valueOf(line.charAt(i)));
        }
        anim = anim.trim();
        for (i = 0; i < calcs.size(); ++i) {
            ((Operation)calcs.get((int)i)).num = ((Operation)calcs.get((int)i)).num.trim();
            if (((Operation)calcs.get((int)i)).num.contains(":cos:")) {
                action.mod = BendsAction.EnumModifier.COS;
                ((Operation)calcs.get((int)i)).num = ((Operation)calcs.get((int)i)).num.replaceAll(":cos:", "");
                ((Operation)calcs.get((int)i)).num = ((Operation)calcs.get((int)i)).num.trim();
            }
            if (((Operation)calcs.get((int)i)).num.contains(":sin:")) {
                action.mod = BendsAction.EnumModifier.SIN;
                ((Operation)calcs.get((int)i)).num = ((Operation)calcs.get((int)i)).num.replaceAll(":sin:", "");
                ((Operation)calcs.get((int)i)).num = ((Operation)calcs.get((int)i)).num.trim();
            }
            if (((Operation)calcs.get((int)i)).num.contains("$")) {
                ((Operation)calcs.get((int)i)).num = ((Operation)calcs.get((int)i)).num.replace("$", " ");
                ((Operation)calcs.get((int)i)).globalVar = ((Operation)calcs.get((int)i)).num = ((Operation)calcs.get((int)i)).num.trim();
                ((Operation)calcs.get((int)i)).num = "0";
                System.out.println("Global Var Used: " + ((Operation)calcs.get((int)i)).globalVar);
            }
            ((Operation)calcs.get((int)i)).operator = ((Operation)calcs.get((int)i)).operator.trim();
            System.out.println("Number: " + ((Operation)calcs.get((int)i)).num + ", " + ((Operation)calcs.get((int)i)).operator + ";");
            System.out.println("Line: " + line);
            action.calculations.add(new BendsAction.Calculation(BendsAction.getOperatorFromSymbol(((Operation)calcs.get((int)i)).operator), Float.parseFloat(((Operation)calcs.get((int)i)).num)).setGlobalVar(((Operation)calcs.get((int)i)).globalVar));
        }
        if (line.contains(":rot:")) {
            action.prop = BendsAction.EnumBoxProperty.ROT;
        } else if (line.contains(":scale:")) {
            action.prop = BendsAction.EnumBoxProperty.SCALE;
        } else if (line.contains(":prerot:")) {
            action.prop = BendsAction.EnumBoxProperty.PREROT;
        }
        if (line.contains(":x")) {
            action.axis = EnumAxis.X;
        } else if (line.contains(":y")) {
            action.axis = EnumAxis.Y;
        } else if (line.contains(":z")) {
            action.axis = EnumAxis.Z;
        }
        action.smooth = Float.parseFloat(smooth);
        return action;
    }

    public static BendsTarget getTargetByID(String argID) {
        for (int i = 0; i < targets.size(); ++i) {
            if (!BendsPack.targets.get((int)i).mob.equalsIgnoreCase(argID)) continue;
            return targets.get(i);
        }
        return null;
    }

    public static void animate(ModelBendsPlayer model, String target, String anim) {
        if (BendsPack.getTargetByID(target) == null) {
            return;
        }
        BendsPack.getTargetByID(target).applyToModel((ModelRendererBends)model.bipedBody, anim, "body");
        BendsPack.getTargetByID(target).applyToModel((ModelRendererBends)model.bipedHead, anim, "head");
        BendsPack.getTargetByID(target).applyToModel((ModelRendererBends)model.bipedLeftArm, anim, "leftArm");
        BendsPack.getTargetByID(target).applyToModel((ModelRendererBends)model.bipedRightArm, anim, "rightArm");
        BendsPack.getTargetByID(target).applyToModel((ModelRendererBends)model.bipedLeftLeg, anim, "leftLeg");
        BendsPack.getTargetByID(target).applyToModel((ModelRendererBends)model.bipedRightLeg, anim, "rightLeg");
        BendsPack.getTargetByID(target).applyToModel((ModelRendererBends)model.bipedLeftForeArm, anim, "leftForeArm");
        BendsPack.getTargetByID(target).applyToModel((ModelRendererBends)model.bipedRightForeArm, anim, "rightForeArm");
        BendsPack.getTargetByID(target).applyToModel((ModelRendererBends)model.bipedLeftForeLeg, anim, "leftForeLeg");
        BendsPack.getTargetByID(target).applyToModel((ModelRendererBends)model.bipedRightForeLeg, anim, "rightForeLeg");
    }

    public static void animate(ModelBendsZombie model, String target, String anim) {
        if (BendsPack.getTargetByID(target) == null) {
            return;
        }
        BendsPack.getTargetByID(target).applyToModel((ModelRendererBends)model.bipedBody, anim, "body");
        BendsPack.getTargetByID(target).applyToModel((ModelRendererBends)model.bipedHead, anim, "head");
        BendsPack.getTargetByID(target).applyToModel((ModelRendererBends)model.bipedLeftArm, anim, "leftArm");
        BendsPack.getTargetByID(target).applyToModel((ModelRendererBends)model.bipedRightArm, anim, "rightArm");
        BendsPack.getTargetByID(target).applyToModel((ModelRendererBends)model.bipedLeftLeg, anim, "leftLeg");
        BendsPack.getTargetByID(target).applyToModel((ModelRendererBends)model.bipedRightLeg, anim, "rightLeg");
        BendsPack.getTargetByID(target).applyToModel((ModelRendererBends)model.bipedLeftForeArm, anim, "leftForeArm");
        BendsPack.getTargetByID(target).applyToModel((ModelRendererBends)model.bipedRightForeArm, anim, "rightForeArm");
        BendsPack.getTargetByID(target).applyToModel((ModelRendererBends)model.bipedLeftForeLeg, anim, "leftForeLeg");
        BendsPack.getTargetByID(target).applyToModel((ModelRendererBends)model.bipedRightForeLeg, anim, "rightForeLeg");
    }

    public static void animate(ModelBendsSpider model, String target, String anim) {
        if (BendsPack.getTargetByID(target) == null) {
            return;
        }
        BendsPack.getTargetByID(target).applyToModel((ModelRendererBends)model.spiderBody, anim, "body");
        BendsPack.getTargetByID(target).applyToModel((ModelRendererBends)model.spiderNeck, anim, "neck");
        BendsPack.getTargetByID(target).applyToModel((ModelRendererBends)model.spiderHead, anim, "head");
        BendsPack.getTargetByID(target).applyToModel((ModelRendererBends)model.spiderLeg1, anim, "leg1");
        BendsPack.getTargetByID(target).applyToModel((ModelRendererBends)model.spiderLeg2, anim, "leg2");
        BendsPack.getTargetByID(target).applyToModel((ModelRendererBends)model.spiderLeg3, anim, "leg3");
        BendsPack.getTargetByID(target).applyToModel((ModelRendererBends)model.spiderLeg4, anim, "leg4");
        BendsPack.getTargetByID(target).applyToModel((ModelRendererBends)model.spiderLeg5, anim, "leg5");
        BendsPack.getTargetByID(target).applyToModel((ModelRendererBends)model.spiderLeg6, anim, "leg6");
        BendsPack.getTargetByID(target).applyToModel((ModelRendererBends)model.spiderLeg7, anim, "leg7");
        BendsPack.getTargetByID(target).applyToModel((ModelRendererBends)model.spiderLeg8, anim, "leg8");
        BendsPack.getTargetByID(target).applyToModel(model.spiderForeLeg1, anim, "foreLeg1");
        BendsPack.getTargetByID(target).applyToModel(model.spiderForeLeg2, anim, "foreLeg2");
        BendsPack.getTargetByID(target).applyToModel(model.spiderForeLeg3, anim, "foreLeg3");
        BendsPack.getTargetByID(target).applyToModel(model.spiderForeLeg4, anim, "foreLeg4");
        BendsPack.getTargetByID(target).applyToModel(model.spiderForeLeg5, anim, "foreLeg5");
        BendsPack.getTargetByID(target).applyToModel(model.spiderForeLeg6, anim, "foreLeg7");
        BendsPack.getTargetByID(target).applyToModel(model.spiderForeLeg7, anim, "foreLeg7");
        BendsPack.getTargetByID(target).applyToModel(model.spiderForeLeg8, anim, "foreLeg8");
    }

    public void save() throws IOException {
        String tab = "\t";
        BendsLogger.log("Saving Pack " + this.displayName + "...", BendsLogger.DEBUG);
        if (this.filename == null) {
            this.filename = BendsPack.constructFilenameWithDisplayName(this.displayName);
        }
        int s = 0;
        while (true) {
            if (s >= targets.size()) break;
            BendsLogger.log("    -" + BendsPack.targets.get((int)s).actions.size(), BendsLogger.DEBUG);
            ++s;
        }
        File packFile = new File(bendsPacksDir, this.filename + "");
        packFile.createNewFile();
        BufferedWriter os = new BufferedWriter(new FileWriter(packFile));
        os.write("name: \"" + this.displayName + "\"\n");
        os.write("author: \"" + this.author + "\"\n");
        os.write("description: \"" + this.description + "\"\n");
        os.newLine();
        int t = 0;
        while (true) {
            if (t >= targets.size()) break;
            BendsTarget target = targets.get(t);
            os.write("target " + target.mob + " {\n");
            String anim = null;
            for (int a = 0; a < target.actions.size(); ++a) {
                BendsAction action = target.actions.get(a);
                if (action.calculations.size() <= 0) continue;
                if (anim == null || !anim.equalsIgnoreCase(action.anim)) {
                    if (anim != null) {
                        os.write(tab + "}\n");
                    }
                    os.write(tab + "anim " + action.anim + " {\n");
                    anim = action.anim;
                }
                os.write(tab + tab + "@" + action.model + ":" + (action.prop == BendsAction.EnumBoxProperty.ROT ? "rot" : (action.prop == BendsAction.EnumBoxProperty.SCALE ? "scale" : "prerot")) + ":" + (action.axis == EnumAxis.X ? "x" : (action.axis == EnumAxis.Y ? "y" : (action.axis == EnumAxis.Z ? "z" : ""))) + " ");
                for (int c = 0; c < action.calculations.size(); ++c) {
                    BendsAction.Calculation calc = action.calculations.get(c);
                    os.write(calc.operator == BendsAction.EnumOperator.SET ? "==" : (calc.operator == BendsAction.EnumOperator.ADD ? "+=" : (calc.operator == BendsAction.EnumOperator.SUBSTRACT ? "-=" : (calc.operator == BendsAction.EnumOperator.MULTIPLY ? "*=" : (calc.operator == BendsAction.EnumOperator.DIVIDE ? "/=" : "==")))));
                    if (c == 0) {
                        os.write(" " + (action.mod == BendsAction.EnumModifier.COS ? ":cos:" : (action.mod == BendsAction.EnumModifier.SIN ? ":sin:" : "")));
                    }
                    os.write(calc.globalVar == null ? "" + calc.number : "$" + calc.globalVar);
                }
                os.write(" #" + action.smooth);
                os.newLine();
                if (a != target.actions.size() - 1) continue;
                os.write(tab + "}\n");
            }
            os.write("}\n\n");
            ++t;
        }
        os.close();
    }

    public static String constructFilenameWithDisplayName(String argName) {
        String filename = argName;
        filename = filename.toLowerCase();
        filename = filename.replace('.', ' ');
        filename = filename.trim();
        filename = filename.replace(" ", "_");
        return filename + ".bends";
    }

    static {
        bendsPacks = new ArrayList<BendsPack>();
        currentPack = 0;
        targets = new ArrayList<BendsTarget>();
    }
}

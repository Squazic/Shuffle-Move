/*  ShuffleMove - A program for identifying and simulating ideal moves in the game
 *  called Pokemon Shuffle.
 *  
 *  Copyright (C) 2015  Andrew Meyers
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package shuffle.fwk.config.manager;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import shuffle.fwk.config.ConfigEntry;
import shuffle.fwk.config.ConfigFactory;
import shuffle.fwk.config.ConfigManager;
import shuffle.fwk.config.ConfigParser;
import shuffle.fwk.config.EntryType;
import shuffle.fwk.config.interpreter.RosterConfigInterpreter;
import shuffle.fwk.data.Effect;
import shuffle.fwk.data.Species;

/**
 * @author Andrew Meyers
 *         
 */
public class RosterManager extends ConfigManager {
   
   private static final String SPEEDUP_FORMAT = "SPEEDUP_%s";
   private static final String SKILL_FORMAT_LEGACY = "SKILL_%s";
   private static final String SKILL_FORMAT = "SKILL_%s_%s";
   private static final String CHOSEN_EFFECT = "EFFECT_%s";
   
   public RosterManager(List<String> loadPaths, List<String> writePaths, ConfigFactory factory) {
      super(loadPaths, writePaths, factory);
   }
   
   public RosterManager(ConfigManager manager) {
      super(manager);
   }
   
   public Integer getLevelForSpecies(Species species) {
      return getLevelForSpecies(species.getName());
   }
   
   public Integer getLevelForSpecies(String speciesName) {
      return getIntegerValue(speciesName, 0);
   }
   
   public boolean setLevelForSpecies(Species species, int level) {
      return setLevelForSpecies(species.getName(), level);
   }
   
   public boolean setLevelForSpecies(String species, int level) {
      return setEntry(EntryType.INTEGER, species, level);
   }
   
   public Integer getSkillLevelForSpecies(Species species) {
      Integer ret = getIntegerValue(getSkillKey(species));
      if (ret == null) {
         ret = getIntegerValue(getSkillKeyLegacy(species));
         if (ret != null) {
            setSkillLevelForSpecies(species, ret.intValue());
            removeEntry(EntryType.INTEGER, getSkillKeyLegacy(species));
         }
      }
      return ret == null ? 1 : ret;
   }
   
   public boolean setSkillLevelForSpecies(Species species, int level) {
      boolean changed;
      level = Math.max(Math.min(level, 5), 1);
      if (level == 1) {
         changed = removeEntry(EntryType.INTEGER, getSkillKey(species));
      } else {
         changed = setEntry(EntryType.INTEGER, getSkillKey(species), level);
      }
      return changed;
   }
   
   private String getSkillKey(Species species) {
      return String.format(SKILL_FORMAT, species.getName(), getActiveEffect(species).toString());
   }
   
   private String getSkillKeyLegacy(Species species) {
      return String.format(SKILL_FORMAT_LEGACY, species.getName());
   }
   
   public Integer getMegaThresholdFor(Species species, EffectManager effectManager) {
      if (species == null || species.getMegaName() == null) {
         return Integer.MAX_VALUE;
      } else {
         return effectManager.getMegaThreshold(species) - getMegaSpeedupsFor(species);
      }
   }
   
   /**
    * @param species
    * @return The Number of Speedups that the user has for a given species.
    */
   public int getMegaSpeedupsFor(Species species) {
      return getIntegerValue(getSpeedupsKey(species), 0);
   }
   
   /**
    * @param species
    * @return
    */
   private String getSpeedupsKey(Species species) {
      return String.format(SPEEDUP_FORMAT, species.getMegaName());
   }
   
   public boolean setMegaSpeedupsFor(Species species, int num) {
      boolean changed;
      if (num == 0) {
         changed = removeEntry(EntryType.INTEGER, getSpeedupsKey(species));
      } else {
         changed = setEntry(EntryType.INTEGER, getSpeedupsKey(species), num);
      }
      return changed;
   }
   
   public static void main(String[] args) {
      // migrateFrom("config/roster.txt");
   }
   
   public static void migrateFrom(String filePath, ConfigFactory factory) {
      ConfigManager manager = factory.getRosterManager();
      migrateUsing(filePath, manager);
      manager.saveDataToConfig();
   }
   
   public static void migrateUsing(String filePath, ConfigManager manager) {
      if (isLegacyFile(filePath)) {
         Map<String, List<Integer>> parsedResults = ConfigParser.parseFile(filePath, new RosterConfigInterpreter());
         for (String key : parsedResults.keySet()) {
            for (Integer value : parsedResults.get(key)) {
               if (value != null) {
                  ConfigEntry entry = new ConfigEntry(EntryType.INTEGER, value);
                  manager.setEntry(EntryType.INTEGER, key, entry);
                  break;
               }
            }
         }
      } else {
         RosterManager rm = new RosterManager(null, Arrays.asList(filePath), manager.getFactory());
         rm.loadFromConfig();
         manager.copyFromManager(rm);
      }
   }
   
   /**
    * @param filePath
    * @return
    */
   private static boolean isLegacyFile(String filePath) {
      return filePath.endsWith("roster.txt");
   }
   
   /**
    * @param species
    * @return
    */
   public Effect getActiveEffect(Species species) {
      String activeEffect = getStringValue(getActiveEffectKey(species));
      Effect ret = species.getDefaultEffect();
      if (activeEffect != null && species.hasEffect(activeEffect)) {
         ret = Effect.valueOf(activeEffect);
      }
      return ret;
   }
   
   /**
    * Sets the "active" effect for the given species to the given effect. If effect is null it will
    * un-set the effect. This will cause no change if the species is null, or if the effect is
    * non-null and is not configured for the species. Also, if the effect given is the default
    * effect, it will be treated as null.
    * 
    * @param species
    * @param effect
    * @return true if anything changed, false if otherwise
    */
   public boolean setActiveEffect(Species species, Effect effect) {
      if (species == null || effect != null && !species.getEffects().contains(effect)) {
         return false;
      }
      String key = getActiveEffectKey(species);
      String value = effect == null || species.getDefaultEffect().equals(effect) ? null : effect.toString();
      return setEntry(EntryType.STRING, key, value);
   }
   
   /**
    * @param species
    * @return
    */
   private String getActiveEffectKey(Species species) {
      return String.format(CHOSEN_EFFECT, species.getName());
   }
   
}

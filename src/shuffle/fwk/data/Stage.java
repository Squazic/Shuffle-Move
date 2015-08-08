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

package shuffle.fwk.data;

import java.util.Collection;
import java.util.HashSet;
import java.util.Queue;

/**
 * @author Andrew Meyers
 *
 */
public class Stage {
   
   private final String stageName;
   private final String targetName;
   private final PkmType targetType;
   
   public Stage(Stage stage) {
      stageName = stage.stageName;
      targetName = stage.targetName;
      targetType = stage.targetType;
   }
   
   public Stage(String name, String target, PkmType type) {
      stageName = name;
      targetName = target;
      targetType = type;
   }
   
   public Stage(String name, Species target, boolean isMega) {
      stageName = name;
      targetName = isMega && target.getMegaName() != null ? target.getMegaName() : target.getName();
      targetType = target.getType();
   }
   
   public Stage(String name, PkmType target) {
      stageName = name;
      targetName = target.toString(); // TODO make nice with WordUtils.capitalizeFully()
      targetType = target;
   }
   
   public Stage(PkmType target) {
      this(target.toString(), target);
   }
   
   // TODO implement stage-specific configuration for predictable stages.
   public Collection<Queue<Species>> getDropPatterns(int column) {
      return new HashSet<Queue<Species>>();
   }
   
   public String getName() {
      return stageName;
   }
   
   public String getTarget() {
      return targetName;
   }
   
   public PkmType getType() {
      return targetType;
   }
   
   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder();
      if (targetType.toString().equals(targetName)) {
         sb.append(targetName);
      } else {
         sb.append(stageName);
         sb.append(": ");
         sb.append(targetName);
      }
      return sb.toString();
   }
   
   @Override
   public int hashCode() {
      return toString().hashCode();
   }
   
   @Override
   public boolean equals(Object o) {
      return o != null && o.toString().equals(toString());
   }
}
package com.gregtechceu.gtceu.compat.atm10;

import com.gregtechceu.gtceu.GTCEu;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.neoforged.fml.loading.FMLPaths;

public final class GregStarQuestInstaller {
   static final String LEGACY_CHAPTER = "chapters/gregstar.snbt";
   static final String LEGACY_DISABLED_SUFFIX = ".disabled-by-gtceu-atm10";
   static final String MIGRATED_BACKUP_SUFFIX = ".pre-gtceu-item-migration";
   static final String OBSOLETE_MANAGED_SUFFIX = ".obsolete-by-gtceu-atm10";
   private static final String KNOWN_LEGACY_CHAPTER_SHA256 = "d7371f8199cead38aeb7fc471b64da28e31881a898468cd07dfc18a3aa7485a0";
   static final String STRUCTURE_MATERIALS_QUEST_ID = "4C33366BAD0256CF";
   static final String BLACK_HOLE_CONTROLLER_QUEST_ID = "3BF90C250AC1ADF3";
   static final List<GregStarQuestInstaller.TaskParity> STRUCTURE_MATERIAL_TASKS = List.of(
      new GregStarQuestInstaller.TaskParity("382025000E17124F", "allthecompressed:atm_star_block_2x", 16L, false),
      new GregStarQuestInstaller.TaskParity("3E9BF312DCCD0CBD", "gtceu:fusion_casing_mk3", 134L, true),
      new GregStarQuestInstaller.TaskParity("6A83E5C44427C560", "gtceu:fusion_glass", 44L, true),
      new GregStarQuestInstaller.TaskParity("69FDB28A4D98F573", "gtceu:superconducting_coil", 600L, true),
      new GregStarQuestInstaller.TaskParity("241FE00A20514BBE", "gtceu:atomic_casing", 1L, false),
      new GregStarQuestInstaller.TaskParity("32DB5277664073F0", "gtceu:micro_universe_energy_transmitter", 160L, false),
      new GregStarQuestInstaller.TaskParity("18324DF3EDCE5200", "gtceu:micro_universe_focus_lens", 28L, false),
      new GregStarQuestInstaller.TaskParity("22BD6AEC9CA35673", "connectedglass:clear_glass_black", 768L, true)
   );
   static final GregStarQuestInstaller.TaskParity BLACK_HOLE_CONTROLLER_TASK = new GregStarQuestInstaller.TaskParity(
      "2F81C3902406C1AE", "gtceu:black_hole_controller", 8L, false
   );
   static final List<GregStarQuestInstaller.QuestFileMigration> KNOWN_QUEST_FILE_MIGRATIONS = List.of(
      new GregStarQuestInstaller.QuestFileMigration(
         "chapters/insane_voltage.snbt", new GregStarQuestInstaller.ItemMigration("kubejs:raw_fluorite", "gtceu:raw_fluorite")
      ),
      new GregStarQuestInstaller.QuestFileMigration(
         "chapters/extreme_voltage.snbt", new GregStarQuestInstaller.ItemMigration("kubejs:medium_oil_cell", "gtceu:oil_bucket")
      ),
      new GregStarQuestInstaller.QuestFileMigration(
         "chapters/ultra_high_voltage.snbt", new GregStarQuestInstaller.ItemMigration("kubejs:trinaquadalloy_cell", "gtceu:trinaquadalloy_bucket")
      ),
      new GregStarQuestInstaller.QuestFileMigration(
         "chapters/ultra_high_voltage.snbt", new GregStarQuestInstaller.ItemMigration("kubejs:star_matter_plasma_cell", "gtceu:star_matter_plasma_bucket")
      )
   );
   static final List<GregStarQuestInstaller.ItemMigration> ITEM_MIGRATIONS = List.of(
      new GregStarQuestInstaller.ItemMigration("kubejs:star_housing", "gtceu:star_housing"),
      new GregStarQuestInstaller.ItemMigration("kubejs:absolute_reaction_plating", "gtceu:absolute_reaction_plating"),
      new GregStarQuestInstaller.ItemMigration("kubejs:star_compression_module", "gtceu:star_compression_module"),
      new GregStarQuestInstaller.ItemMigration("kubejs:superthermal_transference_coil", "gtceu:superthermal_transference_coil"),
      new GregStarQuestInstaller.ItemMigration("kubejs:cable_of_hyperconductivity", "gtceu:cable_of_hyperconductivity"),
      new GregStarQuestInstaller.ItemMigration("kubejs:greg_star_shard", "gtceu:greg_star_shard"),
      new GregStarQuestInstaller.ItemMigration("kubejs:doge_coin", "gtceu:doge_coin"),
      new GregStarQuestInstaller.ItemMigration("kubejs:micro_universe_catalyst", "gtceu:micro_universe_catalyst"),
      new GregStarQuestInstaller.ItemMigration("kubejs:micro_universe_energy_transmitter", "gtceu:micro_universe_energy_transmitter"),
      new GregStarQuestInstaller.ItemMigration("kubejs:micro_universe_focus_lens", "gtceu:micro_universe_focus_lens"),
      new GregStarQuestInstaller.ItemMigration("kubejs:inert_nether_star", "gtceu:inert_nether_star"),
      new GregStarQuestInstaller.ItemMigration("kubejs:creative_polonium_cell", "gtceu:creative_polonium_cell")
   );
   private static final List<String> OBSOLETE_MANAGED_FILES = List.of(
      "chapters/gtceu_gregstar.snbt", "lang/en_us/chapters/gtceu_gregstar.snbt_merged", "lang/zh_cn/chapters/gtceu_gregstar.snbt_merged"
   );

   private GregStarQuestInstaller() {
   }

   public static void installIfNeeded() {
      if (!GTCEu.isDataGen() && GTCEu.Mods.isFTBQuestsLoaded() && GTCEu.Mods.isKubeJSLoaded() && Atm10Environment.isDetected()) {
         Path questRoot = FMLPaths.CONFIGDIR.get().resolve("ftbquests/quests").normalize();

         try {
            int obsoleteManaged = quarantineObsoleteManagedFiles(questRoot);
            GregStarQuestInstaller.RestoreResult restored = restoreKnownLegacyChapter(questRoot);
            GregStarQuestInstaller.MigrationResult migrated = migrateLegacyChapter(questRoot);
            GregStarQuestInstaller.QuestFilesMigrationResult voltage = migrateKnownQuestFiles(questRoot);
            if (obsoleteManaged > 0 || restored != GregStarQuestInstaller.RestoreResult.ACTIVE_PRESENT || migrated.changed() || voltage.changedFiles() > 0) {
               GTCEu.LOGGER
                  .info(
                     "GregStar task-line migration: quarantined_managed={}, restored_original={}, replaced_item_references={}, repaired_task_objects={}, backup_created={}, voltage_files={}, voltage_references={}, voltage_backups={}",
                     obsoleteManaged,
                     restored,
                     migrated.replacedReferences(),
                     migrated.repairedTaskObjects(),
                     migrated.backupCreated(),
                     voltage.changedFiles(),
                     voltage.replacedReferences(),
                     voltage.backupsCreated()
                  );
            }
         } catch (Exception var5) {
            GTCEu.LOGGER.error("Failed to migrate ATM10's original GregStar task line", var5);
         }
      }
   }

   static GregStarQuestInstaller.RestoreResult restoreKnownLegacyChapter(Path questRoot) throws IOException {
      Path root = questRoot.toAbsolutePath().normalize();
      Path active = safeResolve(root, "chapters/gregstar.snbt");
      if (Files.isRegularFile(active)) {
         return GregStarQuestInstaller.RestoreResult.ACTIVE_PRESENT;
      } else {
         Path disabled = active.resolveSibling(active.getFileName() + ".disabled-by-gtceu-atm10");
         if (!Files.isRegularFile(disabled)) {
            return GregStarQuestInstaller.RestoreResult.ABSENT;
         } else {
            byte[] bytes = Files.readAllBytes(disabled);
            if (!isKnownLegacyChapter(bytes)) {
               return GregStarQuestInstaller.RestoreResult.UNRECOGNIZED_BACKUP;
            } else {
               Files.createDirectories(active.getParent());
               atomicWrite(active, bytes);
               return GregStarQuestInstaller.RestoreResult.RESTORED;
            }
         }
      }
   }

   static GregStarQuestInstaller.MigrationText migrateItemIds(String text) {
      return migrateItemIds(text, ITEM_MIGRATIONS);
   }

   private static GregStarQuestInstaller.MigrationText migrateItemIds(String text, List<GregStarQuestInstaller.ItemMigration> migrations) {
      String migrated = text;
      int replaced = 0;

      for (GregStarQuestInstaller.ItemMigration migration : migrations) {
         Pattern pattern = Pattern.compile("(?m)(\\bid\\s*:\\s*\")" + Pattern.quote(migration.legacyId()) + "(\")");
         Matcher matcher = pattern.matcher(migrated);

         StringBuffer buffer;
         for (buffer = new StringBuffer(); matcher.find(); replaced++) {
            matcher.appendReplacement(buffer, Matcher.quoteReplacement(matcher.group(1) + migration.canonicalId() + matcher.group(2)));
         }

         matcher.appendTail(buffer);
         migrated = buffer.toString();
      }

      return new GregStarQuestInstaller.MigrationText(migrated, replaced);
   }

   static GregStarQuestInstaller.QuestFilesMigrationResult migrateKnownQuestFiles(Path questRoot) throws IOException {
      Path root = questRoot.toAbsolutePath().normalize();
      int changedFiles = 0;
      int replacedReferences = 0;
      int backupsCreated = 0;
      Map<String, List<GregStarQuestInstaller.ItemMigration>> migrationsByFile = new LinkedHashMap<>();

      for (GregStarQuestInstaller.QuestFileMigration specification : KNOWN_QUEST_FILE_MIGRATIONS) {
         migrationsByFile.computeIfAbsent(specification.relativePath(), ignored -> new ArrayList<>()).add(specification.migration());
      }

      for (Entry<String, List<GregStarQuestInstaller.ItemMigration>> entry : migrationsByFile.entrySet()) {
         Path active = safeResolve(root, entry.getKey());
         if (Files.isRegularFile(active)) {
            byte[] currentBytes = Files.readAllBytes(active);
            String current = new String(currentBytes, StandardCharsets.UTF_8);
            GregStarQuestInstaller.MigrationText migration = migrateItemIds(current, entry.getValue());
            if (!migration.text().equals(current)) {
               Path backup = active.resolveSibling(active.getFileName() + ".pre-gtceu-item-migration");
               boolean backupCreated = false;
               if (!Files.exists(backup)) {
                  atomicWrite(backup, currentBytes);
                  backupCreated = true;
               }

               atomicWrite(active, migration.text().getBytes(StandardCharsets.UTF_8));
               changedFiles++;
               replacedReferences += migration.replacedReferences();
               if (backupCreated) {
                  backupsCreated++;
               }
            }
         }
      }

      return new GregStarQuestInstaller.QuestFilesMigrationResult(changedFiles, replacedReferences, backupsCreated);
   }

   static GregStarQuestInstaller.QuestParityText repairKnownQuestParity(String text) {
      GregStarQuestInstaller.QuestEdit structureMaterials = repairStructureMaterialsQuest(text);
      GregStarQuestInstaller.QuestEdit blackHoleController = repairSingleKnownTask(structureMaterials.text(), "3BF90C250AC1ADF3", BLACK_HOLE_CONTROLLER_TASK);
      return new GregStarQuestInstaller.QuestParityText(
         blackHoleController.text(), structureMaterials.repairedTaskObjects() + blackHoleController.repairedTaskObjects()
      );
   }

   private static GregStarQuestInstaller.QuestEdit repairStructureMaterialsQuest(String original) {
      GregStarQuestInstaller.TaskList taskList = findTaskList(original, "4C33366BAD0256CF");
      if (taskList == null) {
         return GregStarQuestInstaller.QuestEdit.unchanged(original);
      } else {
         int previousStart = -1;

         for (GregStarQuestInstaller.TaskParity task : STRUCTURE_MATERIAL_TASKS) {
            List<GregStarQuestInstaller.ObjectRange> matches = findTasksById(original, taskList, task.taskId());
            if (matches.size() <= 1 && (!matches.isEmpty() || task.restoreIfMissing())) {
               if (matches.isEmpty()) {
                  continue;
               }

               GregStarQuestInstaller.ObjectRange match = matches.getFirst();
               if (match.start() >= previousStart && isItemTask(original, match)) {
                  previousStart = match.start();
                  continue;
               }

               return GregStarQuestInstaller.QuestEdit.unchanged(original);
            }

            return GregStarQuestInstaller.QuestEdit.unchanged(original);
         }

         String repaired = original;
         Set<String> changedTaskIds = new LinkedHashSet<>();

         for (GregStarQuestInstaller.TaskParity task : STRUCTURE_MATERIAL_TASKS) {
            GregStarQuestInstaller.TaskList currentList = findTaskList(repaired, "4C33366BAD0256CF");
            if (currentList == null) {
               return GregStarQuestInstaller.QuestEdit.unchanged(original);
            }

            List<GregStarQuestInstaller.ObjectRange> matches = findTasksById(repaired, currentList, task.taskId());
            if (matches.isEmpty()) {
               GregStarQuestInstaller.TextEdit insertion = insertMissingTask(repaired, task);
               if (!insertion.success()) {
                  return GregStarQuestInstaller.QuestEdit.unchanged(original);
               }

               repaired = insertion.text();
               changedTaskIds.add(task.taskId());
            }
         }

         for (GregStarQuestInstaller.TaskParity task : STRUCTURE_MATERIAL_TASKS) {
            GregStarQuestInstaller.TextEdit edit = repairExistingTask(repaired, "4C33366BAD0256CF", task);
            if (!edit.success()) {
               return GregStarQuestInstaller.QuestEdit.unchanged(original);
            }

            repaired = edit.text();
            if (edit.changed()) {
               changedTaskIds.add(task.taskId());
            }
         }

         return new GregStarQuestInstaller.QuestEdit(repaired, changedTaskIds.size());
      }
   }

   private static GregStarQuestInstaller.QuestEdit repairSingleKnownTask(String original, String questId, GregStarQuestInstaller.TaskParity task) {
      GregStarQuestInstaller.TextEdit edit = repairExistingTask(original, questId, task);
      return edit.success() && edit.changed() ? new GregStarQuestInstaller.QuestEdit(edit.text(), 1) : GregStarQuestInstaller.QuestEdit.unchanged(original);
   }

   private static GregStarQuestInstaller.TextEdit insertMissingTask(String text, GregStarQuestInstaller.TaskParity missingTask) {
      GregStarQuestInstaller.TaskList taskList = findTaskList(text, "4C33366BAD0256CF");
      if (taskList == null) {
         return GregStarQuestInstaller.TextEdit.failure(text);
      } else {
         int taskIndex = STRUCTURE_MATERIAL_TASKS.indexOf(missingTask);
         GregStarQuestInstaller.ObjectRange next = null;

         for (int index = taskIndex + 1; index < STRUCTURE_MATERIAL_TASKS.size(); index++) {
            List<GregStarQuestInstaller.ObjectRange> matches = findTasksById(text, taskList, STRUCTURE_MATERIAL_TASKS.get(index).taskId());
            if (matches.size() > 1) {
               return GregStarQuestInstaller.TextEdit.failure(text);
            }

            if (!matches.isEmpty()) {
               next = matches.getFirst();
               break;
            }
         }

         GregStarQuestInstaller.ObjectRange previous = null;

         for (int index = taskIndex - 1; index >= 0; index--) {
            List<GregStarQuestInstaller.ObjectRange> matchesx = findTasksById(text, taskList, STRUCTURE_MATERIAL_TASKS.get(index).taskId());
            if (matchesx.size() > 1) {
               return GregStarQuestInstaller.TextEdit.failure(text);
            }

            if (!matchesx.isEmpty()) {
               previous = matchesx.getFirst();
               break;
            }
         }

         GregStarQuestInstaller.ObjectRange styleSource = next != null ? next : previous;
         if (styleSource == null) {
            return GregStarQuestInstaller.TextEdit.failure(text);
         } else {
            String objectIndent = lineIndentAt(text, styleSource.start());
            GregStarQuestInstaller.PropertyValue styleId = findTopLevelProperty(text, styleSource, "id");
            String propertyIndent = styleId == null ? null : lineIndentAt(text, styleId.keyStart());
            if (objectIndent != null && propertyIndent != null && propertyIndent.length() > objectIndent.length()) {
               String newline = text.contains("\r\n") ? "\r\n" : "\n";
               String taskBlock = formatTaskBlock(missingTask, newline, objectIndent, propertyIndent);
               if (next != null) {
                  GregStarQuestInstaller.ObjectRange immediatePrevious = previousObject(taskList.objects(), next);
                  int gapStart = immediatePrevious == null ? taskList.openBracket() + 1 : immediatePrevious.end();
                  return !isWhitespaceOnly(text, gapStart, next.start())
                     ? GregStarQuestInstaller.TextEdit.failure(text)
                     : GregStarQuestInstaller.TextEdit.changed(
                        text.substring(0, next.start()) + taskBlock + newline + objectIndent + text.substring(next.start())
                     );
               } else {
                  GregStarQuestInstaller.ObjectRange immediateNext = nextObject(taskList.objects(), previous);
                  int gapEnd = immediateNext == null ? taskList.closeBracket() : immediateNext.start();
                  return !isWhitespaceOnly(text, previous.end(), gapEnd)
                     ? GregStarQuestInstaller.TextEdit.failure(text)
                     : GregStarQuestInstaller.TextEdit.changed(
                        text.substring(0, previous.end()) + newline + objectIndent + taskBlock + text.substring(previous.end())
                     );
               }
            } else {
               return GregStarQuestInstaller.TextEdit.failure(text);
            }
         }
      }
   }

   private static String formatTaskBlock(GregStarQuestInstaller.TaskParity task, String newline, String objectIndent, String propertyIndent) {
      return "{"
         + newline
         + propertyIndent
         + "count: "
         + task.count()
         + "L"
         + newline
         + propertyIndent
         + "id: \""
         + task.taskId()
         + "\""
         + newline
         + propertyIndent
         + "item: { count: 1, id: \""
         + task.itemId()
         + "\" }"
         + newline
         + propertyIndent
         + "type: \"item\""
         + newline
         + objectIndent
         + "}";
   }

   private static GregStarQuestInstaller.TextEdit repairExistingTask(String text, String questId, GregStarQuestInstaller.TaskParity task) {
      GregStarQuestInstaller.TaskList taskList = findTaskList(text, questId);
      if (taskList == null) {
         return GregStarQuestInstaller.TextEdit.failure(text);
      } else {
         List<GregStarQuestInstaller.ObjectRange> matches = findTasksById(text, taskList, task.taskId());
         if (matches.size() == 1 && isItemTask(text, matches.getFirst())) {
            GregStarQuestInstaller.TextEdit itemEdit = replaceTaskItem(text, matches.getFirst(), task.itemId());
            if (!itemEdit.success()) {
               return GregStarQuestInstaller.TextEdit.failure(text);
            } else {
               GregStarQuestInstaller.TaskList countTaskList = findTaskList(itemEdit.text(), questId);
               if (countTaskList == null) {
                  return GregStarQuestInstaller.TextEdit.failure(text);
               } else {
                  List<GregStarQuestInstaller.ObjectRange> countMatches = findTasksById(itemEdit.text(), countTaskList, task.taskId());
                  if (countMatches.size() != 1) {
                     return GregStarQuestInstaller.TextEdit.failure(text);
                  } else {
                     GregStarQuestInstaller.TextEdit countEdit = replaceTaskCount(itemEdit.text(), countMatches.getFirst(), task.count());
                     if (!countEdit.success()) {
                        return GregStarQuestInstaller.TextEdit.failure(text);
                     } else {
                        boolean changed = itemEdit.changed() || countEdit.changed();
                        return new GregStarQuestInstaller.TextEdit(countEdit.text(), changed, true);
                     }
                  }
               }
            }
         } else {
            return GregStarQuestInstaller.TextEdit.failure(text);
         }
      }
   }

   private static GregStarQuestInstaller.TextEdit replaceTaskItem(String text, GregStarQuestInstaller.ObjectRange task, String expectedItemId) {
      GregStarQuestInstaller.PropertyValue item = findTopLevelProperty(text, task, "item");
      if (item != null && item.valueStart() < task.end()) {
         GregStarQuestInstaller.PropertyValue itemId = item;
         char first = text.charAt(item.valueStart());
         if (first == '{') {
            int close = findMatching(text, item.valueStart(), '{', '}');
            if (close < 0 || close >= task.end()) {
               return GregStarQuestInstaller.TextEdit.failure(text);
            }

            itemId = findTopLevelProperty(text, new GregStarQuestInstaller.ObjectRange(item.valueStart(), close + 1), "id");
            if (itemId == null) {
               return GregStarQuestInstaller.TextEdit.failure(text);
            }
         }

         return replaceQuotedValue(text, itemId.valueStart(), expectedItemId);
      } else {
         return GregStarQuestInstaller.TextEdit.failure(text);
      }
   }

   private static GregStarQuestInstaller.TextEdit replaceTaskCount(String text, GregStarQuestInstaller.ObjectRange task, long expectedCount) {
      GregStarQuestInstaller.PropertyValue count = findTopLevelProperty(text, task, "count");
      if (count == null) {
         GregStarQuestInstaller.PropertyValue id = findTopLevelProperty(text, task, "id");
         String indent = id == null ? null : lineIndentAt(text, id.keyStart());
         if (id != null && indent != null) {
            String newline = text.contains("\r\n") ? "\r\n" : "\n";
            String addition = "count: " + expectedCount + "L" + newline + indent;
            return GregStarQuestInstaller.TextEdit.changed(text.substring(0, id.keyStart()) + addition + text.substring(id.keyStart()));
         } else {
            return GregStarQuestInstaller.TextEdit.failure(text);
         }
      } else {
         int end = count.valueStart();
         if (end < task.end() && (text.charAt(end) == '-' || text.charAt(end) == '+')) {
            end++;
         }

         int digitStart = end;

         while (end < task.end() && Character.isDigit(text.charAt(end))) {
            end++;
         }

         if (end == digitStart) {
            return GregStarQuestInstaller.TextEdit.failure(text);
         } else {
            if (end < task.end() && (text.charAt(end) == 'L' || text.charAt(end) == 'l')) {
               end++;
            }

            String currentLiteral = text.substring(count.valueStart(), end);

            long current;
            try {
               current = Long.parseLong(currentLiteral.replace("L", "").replace("l", ""));
            } catch (NumberFormatException var11) {
               return GregStarQuestInstaller.TextEdit.failure(text);
            }

            return current == expectedCount
               ? GregStarQuestInstaller.TextEdit.unchanged(text)
               : GregStarQuestInstaller.TextEdit.changed(text.substring(0, count.valueStart()) + expectedCount + "L" + text.substring(end));
         }
      }
   }

   private static GregStarQuestInstaller.TextEdit replaceQuotedValue(String text, int valueStart, String expected) {
      if (valueStart >= 0 && valueStart < text.length()) {
         char quote = text.charAt(valueStart);
         if (quote != '"' && quote != '\'') {
            return GregStarQuestInstaller.TextEdit.failure(text);
         } else {
            int end = findStringEnd(text, valueStart);
            if (end < 0) {
               return GregStarQuestInstaller.TextEdit.failure(text);
            } else {
               String current = text.substring(valueStart + 1, end);
               return current.equals(expected)
                  ? GregStarQuestInstaller.TextEdit.unchanged(text)
                  : GregStarQuestInstaller.TextEdit.changed(text.substring(0, valueStart + 1) + expected + text.substring(end));
            }
         }
      } else {
         return GregStarQuestInstaller.TextEdit.failure(text);
      }
   }

   private static boolean isItemTask(String text, GregStarQuestInstaller.ObjectRange task) {
      GregStarQuestInstaller.PropertyValue type = findTopLevelProperty(text, task, "type");
      return type != null && "item".equals(readQuotedValue(text, type.valueStart()));
   }

   private static GregStarQuestInstaller.TaskList findTaskList(String text, String questId) {
      GregStarQuestInstaller.ObjectRange quest = findUniqueObjectById(text, questId);
      if (quest == null) {
         return null;
      } else {
         GregStarQuestInstaller.PropertyValue tasks = findTopLevelProperty(text, quest, "tasks");
         if (tasks != null && tasks.valueStart() < quest.end() && text.charAt(tasks.valueStart()) == '[') {
            int close = findMatching(text, tasks.valueStart(), '[', ']');
            if (close >= 0 && close < quest.end()) {
               List<GregStarQuestInstaller.ObjectRange> objects = findTopLevelObjects(text, tasks.valueStart(), close);
               return objects == null ? null : new GregStarQuestInstaller.TaskList(tasks.valueStart(), close, objects);
            } else {
               return null;
            }
         } else {
            return null;
         }
      }
   }

   private static List<GregStarQuestInstaller.ObjectRange> findTasksById(String text, GregStarQuestInstaller.TaskList taskList, String taskId) {
      List<GregStarQuestInstaller.ObjectRange> matches = new ArrayList<>();

      for (GregStarQuestInstaller.ObjectRange object : taskList.objects()) {
         GregStarQuestInstaller.PropertyValue id = findTopLevelProperty(text, object, "id");
         if (id != null && taskId.equals(readQuotedValue(text, id.valueStart()))) {
            matches.add(object);
         }
      }

      return matches;
   }

   private static GregStarQuestInstaller.ObjectRange findUniqueObjectById(String text, String id) {
      Pattern idPattern = Pattern.compile("\\bid\\s*:\\s*\"" + Pattern.quote(id) + "\"");
      Matcher matcher = idPattern.matcher(text);
      Set<Integer> starts = new LinkedHashSet<>();
      GregStarQuestInstaller.ObjectRange result = null;

      while (matcher.find()) {
         GregStarQuestInstaller.ObjectRange object = findEnclosingObject(text, matcher.start());
         if (object != null && starts.add(object.start())) {
            GregStarQuestInstaller.PropertyValue objectId = findTopLevelProperty(text, object, "id");
            if (objectId != null && id.equals(readQuotedValue(text, objectId.valueStart()))) {
               if (result != null) {
                  return null;
               }

               result = object;
            }
         }
      }

      return result;
   }

   private static GregStarQuestInstaller.ObjectRange findEnclosingObject(String text, int position) {
      List<Integer> stack = new ArrayList<>();

      for (int index = 0; index <= position && index < text.length(); index++) {
         char current = text.charAt(index);
         if (current == '"' || current == '\'') {
            int stringEnd = findStringEnd(text, index);
            if (stringEnd < 0) {
               return null;
            }

            index = stringEnd;
         } else if (current == '{') {
            stack.add(index);
         } else if (current == '}' && !stack.isEmpty()) {
            stack.removeLast();
         }
      }

      if (stack.isEmpty()) {
         return null;
      } else {
         int start = stack.getLast();
         int end = findMatching(text, start, '{', '}');
         return end < 0 ? null : new GregStarQuestInstaller.ObjectRange(start, end + 1);
      }
   }

   private static GregStarQuestInstaller.PropertyValue findTopLevelProperty(String text, GregStarQuestInstaller.ObjectRange object, String property) {
      int nested = 0;
      int index = object.start() + 1;

      while (true) {
         label103: {
            if (index < object.end() - 1) {
               char current = text.charAt(index);
               if (current == '"' || current == '\'') {
                  int stringEnd = findStringEnd(text, index);
                  if (stringEnd < 0 || stringEnd >= object.end()) {
                     return null;
                  }

                  index = stringEnd;
                  break label103;
               }

               if (current == '{' || current == '[') {
                  nested++;
                  break label103;
               }

               if (current != '}' && current != ']') {
                  if (nested == 0 && text.regionMatches(index, property, 0, property.length())) {
                     int before = index - 1;
                     int after = index + property.length();
                     if ((before < object.start() || !isPropertyNameCharacter(text.charAt(before)))
                        && (after >= object.end() || !isPropertyNameCharacter(text.charAt(after)))) {
                        int colon = after;

                        while (colon < object.end() && Character.isWhitespace(text.charAt(colon))) {
                           colon++;
                        }

                        if (colon < object.end() && text.charAt(colon) == ':') {
                           int valueStart = colon + 1;

                           while (valueStart < object.end() && Character.isWhitespace(text.charAt(valueStart))) {
                              valueStart++;
                           }

                           return new GregStarQuestInstaller.PropertyValue(index, valueStart);
                        }
                     }
                  }
                  break label103;
               }

               if (nested != 0) {
                  nested--;
                  break label103;
               }
            }

            return null;
         }

         index++;
      }
   }

   private static boolean isPropertyNameCharacter(char value) {
      return Character.isLetterOrDigit(value) || value == '_';
   }

   private static String readQuotedValue(String text, int valueStart) {
      if (valueStart >= 0 && valueStart < text.length()) {
         char quote = text.charAt(valueStart);
         if (quote != '"' && quote != '\'') {
            return null;
         } else {
            int end = findStringEnd(text, valueStart);
            return end < 0 ? null : text.substring(valueStart + 1, end);
         }
      } else {
         return null;
      }
   }

   private static int findStringEnd(String text, int start) {
      char quote = text.charAt(start);
      boolean escaped = false;

      for (int index = start + 1; index < text.length(); index++) {
         char current = text.charAt(index);
         if (escaped) {
            escaped = false;
         } else if (current == '\\') {
            escaped = true;
         } else if (current == quote) {
            return index;
         }
      }

      return -1;
   }

   private static int findMatching(String text, int start, char open, char close) {
      int depth = 0;

      for (int index = start; index < text.length(); index++) {
         char current = text.charAt(index);
         if (current == '"' || current == '\'') {
            int stringEnd = findStringEnd(text, index);
            if (stringEnd < 0) {
               return -1;
            }

            index = stringEnd;
         } else if (current == open) {
            depth++;
         } else if (current == close) {
            if (--depth == 0) {
               return index;
            }
         }
      }

      return -1;
   }

   private static List<GregStarQuestInstaller.ObjectRange> findTopLevelObjects(String text, int openBracket, int closeBracket) {
      List<GregStarQuestInstaller.ObjectRange> objects = new ArrayList<>();

      for (int index = openBracket + 1; index < closeBracket; index++) {
         char current = text.charAt(index);
         if (current == '"' || current == '\'') {
            int stringEnd = findStringEnd(text, index);
            if (stringEnd < 0 || stringEnd >= closeBracket) {
               return null;
            }

            index = stringEnd;
         } else if (current == '{') {
            int end = findMatching(text, index, '{', '}');
            if (end < 0 || end >= closeBracket) {
               return null;
            }

            objects.add(new GregStarQuestInstaller.ObjectRange(index, end + 1));
            index = end;
         }
      }

      return objects;
   }

   private static GregStarQuestInstaller.ObjectRange previousObject(List<GregStarQuestInstaller.ObjectRange> objects, GregStarQuestInstaller.ObjectRange target) {
      GregStarQuestInstaller.ObjectRange previous = null;

      for (GregStarQuestInstaller.ObjectRange object : objects) {
         if (object.start() >= target.start()) {
            break;
         }

         previous = object;
      }

      return previous;
   }

   private static GregStarQuestInstaller.ObjectRange nextObject(List<GregStarQuestInstaller.ObjectRange> objects, GregStarQuestInstaller.ObjectRange target) {
      for (GregStarQuestInstaller.ObjectRange object : objects) {
         if (object.start() > target.start()) {
            return object;
         }
      }

      return null;
   }

   private static String lineIndentAt(String text, int position) {
      int lineStart = Math.max(text.lastIndexOf(10, Math.max(0, position - 1)) + 1, 0);
      String indent = text.substring(lineStart, position);
      return isWhitespaceOnly(indent, 0, indent.length()) ? indent : null;
   }

   private static boolean isWhitespaceOnly(String text, int start, int end) {
      for (int index = start; index < end; index++) {
         if (!Character.isWhitespace(text.charAt(index))) {
            return false;
         }
      }

      return true;
   }

   static GregStarQuestInstaller.MigrationResult migrateLegacyChapter(Path questRoot) throws IOException {
      Path root = questRoot.toAbsolutePath().normalize();
      Path active = safeResolve(root, "chapters/gregstar.snbt");
      if (!Files.isRegularFile(active)) {
         return new GregStarQuestInstaller.MigrationResult(false, 0, 0, false);
      } else {
         byte[] currentBytes = Files.readAllBytes(active);
         String current = new String(currentBytes, StandardCharsets.UTF_8);
         GregStarQuestInstaller.MigrationText itemMigration = migrateItemIds(current);
         GregStarQuestInstaller.QuestParityText questParity = repairKnownQuestParity(itemMigration.text());
         if (questParity.text().equals(current)) {
            return new GregStarQuestInstaller.MigrationResult(false, 0, 0, false);
         } else {
            Path backup = active.resolveSibling(active.getFileName() + ".pre-gtceu-item-migration");
            boolean backupCreated = false;
            if (!Files.exists(backup)) {
               atomicWrite(backup, currentBytes);
               backupCreated = true;
            }

            atomicWrite(active, questParity.text().getBytes(StandardCharsets.UTF_8));
            return new GregStarQuestInstaller.MigrationResult(true, itemMigration.replacedReferences(), questParity.repairedTaskObjects(), backupCreated);
         }
      }
   }

   static int quarantineObsoleteManagedFiles(Path questRoot) throws IOException {
      Path root = questRoot.toAbsolutePath().normalize();
      int quarantined = 0;

      for (String relative : OBSOLETE_MANAGED_FILES) {
         Path source = safeResolve(root, relative);
         if (Files.isRegularFile(source)) {
            byte[] bytes = Files.readAllBytes(source);
            if (isManagedResource(new String(bytes, StandardCharsets.UTF_8))) {
               Path target = source.resolveSibling(source.getFileName() + ".obsolete-by-gtceu-atm10");
               if (Files.exists(target)) {
                  if (Arrays.equals(bytes, Files.readAllBytes(target))) {
                     Files.delete(source);
                     quarantined++;
                     continue;
                  }

                  target = source.resolveSibling(source.getFileName() + ".obsolete-by-gtceu-atm10-" + digestPrefix(bytes));
               }

               moveAtomically(source, target);
               quarantined++;
            }
         }
      }

      return quarantined;
   }

   private static Path safeResolve(Path root, String relative) throws IOException {
      Path resolved = root.resolve(relative).normalize();
      if (!resolved.startsWith(root)) {
         throw new IOException("Unsafe GregStar quest path: " + relative);
      } else {
         return resolved;
      }
   }

   private static boolean isKnownLegacyChapter(byte[] bytes) {
      return digest(bytes).equalsIgnoreCase("d7371f8199cead38aeb7fc471b64da28e31881a898468cd07dfc18a3aa7485a0");
   }

   private static boolean isManagedResource(String text) {
      return text.contains("gtceu_managed_version: 1");
   }

   private static String digest(byte[] bytes) {
      try {
         return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
      } catch (NoSuchAlgorithmException var2) {
         throw new IllegalStateException("SHA-256 is unavailable", var2);
      }
   }

   private static String digestPrefix(byte[] bytes) {
      return digest(bytes).substring(0, 12);
   }

   private static void moveAtomically(Path source, Path target) throws IOException {
      try {
         Files.move(source, target, StandardCopyOption.ATOMIC_MOVE);
      } catch (AtomicMoveNotSupportedException var3) {
         Files.move(source, target);
      }
   }

   private static void atomicWrite(Path target, byte[] bytes) throws IOException {
      Files.createDirectories(target.getParent());
      Path temporary = target.resolveSibling(target.getFileName() + ".gtceu.tmp");
      Files.write(temporary, bytes);

      try {
         Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
      } catch (AtomicMoveNotSupportedException var7) {
         Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
      } finally {
         Files.deleteIfExists(temporary);
      }
   }

   static record ItemMigration(String legacyId, String canonicalId) {
   }

   static record MigrationResult(boolean changed, int replacedReferences, int repairedTaskObjects, boolean backupCreated) {
   }

   static record MigrationText(String text, int replacedReferences) {
   }

   private static record ObjectRange(int start, int end) {
   }

   private static record PropertyValue(int keyStart, int valueStart) {
   }

   private static record QuestEdit(String text, int repairedTaskObjects) {
      private static GregStarQuestInstaller.QuestEdit unchanged(String text) {
         return new GregStarQuestInstaller.QuestEdit(text, 0);
      }
   }

   static record QuestFileMigration(String relativePath, GregStarQuestInstaller.ItemMigration migration) {
   }

   static record QuestFilesMigrationResult(int changedFiles, int replacedReferences, int backupsCreated) {
   }

   static record QuestParityText(String text, int repairedTaskObjects) {
   }

   static enum RestoreResult {
      ACTIVE_PRESENT,
      RESTORED,
      ABSENT,
      UNRECOGNIZED_BACKUP;
   }

   private static record TaskList(int openBracket, int closeBracket, List<GregStarQuestInstaller.ObjectRange> objects) {
   }

   static record TaskParity(String taskId, String itemId, long count, boolean restoreIfMissing) {
   }

   private static record TextEdit(String text, boolean changed, boolean success) {
      private static GregStarQuestInstaller.TextEdit changed(String text) {
         return new GregStarQuestInstaller.TextEdit(text, true, true);
      }

      private static GregStarQuestInstaller.TextEdit unchanged(String text) {
         return new GregStarQuestInstaller.TextEdit(text, false, true);
      }

      private static GregStarQuestInstaller.TextEdit failure(String text) {
         return new GregStarQuestInstaller.TextEdit(text, false, false);
      }
   }
}

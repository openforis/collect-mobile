package org.openforis.collect.android.databaseschema;

import liquibase.Liquibase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.openforis.collect.android.IdGenerator;
import org.openforis.collect.android.util.persistence.ConnectionCallback;
import org.openforis.collect.android.util.persistence.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.List;

/**
 * @author Daniel Wiell
 */
public class ModelDatabaseSchemaUpdater {
    private static final String LIQUIBASE_CHANGE_LOG = "org/openforis/collect/db/changelog/db.changelog-master.xml";

    public void update(Database db, final liquibase.database.Database liquibaseDatabase) {
        try {
            liquibaseDatabase.setConnection(new JdbcConnection(db.dataSource().getConnection()));
            Liquibase liquibase = new Liquibase(LIQUIBASE_CHANGE_LOG,
                    new ClassLoaderResourceAccessor(), liquibaseDatabase);
            liquibase.update((String) null);
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to setup database schema", e);
        } catch (LiquibaseException e) {
            throw new IllegalStateException("Failed to setup database schema", e);
        } finally {
            close(liquibaseDatabase);
        }

        initData(db); // TODO: Remove...
    }


    private static void initData(Database modelDatabase) {
        modelDatabase.execute(new ConnectionCallback<Object>() {
            public Object execute(Connection connection) throws SQLException {
//                samplingDesign(connection);
//                taxonomy(connection);
//                taxon(connection);
                return null;
            }


            private void samplingDesign(Connection connection) throws SQLException {
                connection.createStatement().execute("DELETE FROM ofc_sampling_design");
                PreparedStatement ps = connection.prepareStatement("" +
                        "INSERT INTO ofc_sampling_design (id, survey_id, level1, level2, location) " +
                        "VALUES (?, ?, ?, ?, ?)");

                String[] clusterIds = {"7_80", "7_81", "8_80", "9_79", "9_83", "10_111", "10_114", "10_117", "10_98", "11_112", "11_78", "11_87", "11_91", "11_96", "12_113", "12_119", "12_82", "13_110", "13_113", "13_115", "13_116", "13_77", "13_86", "13_90", "14_105", "14_118", "14_85", "14_90", "14_92", "14_96", "14_97", "15_117", "15_79", "15_93", "16_101", "16_104", "16_119", "16_75", "16_76", "16_99", "17_100", "17_110", "17_114", "17_115", "17_117", "17_78", "18_102", "18_118", "18_77", "18_85", "18_91", "19_82", "19_95", "19_96", "19_97", "20_100", "20_105", "20_68", "20_70", "20_99", "21_112", "21_113", "21_65", "21_66", "21_91", "22_113", "22_116", "22_119", "22_62", "22_85", "23_72", "23_93", "23_94", "23_99", "24_100", "24_107", "24_121", "24_125", "24_127", "24_88", "25_103", "25_108", "25_118", "25_129", "25_2", "25_37", "25_70", "25_82", "25_83", "25_89", "25_90", "26_121", "26_128", "26_36", "26_41", "26_43", "26_78", "27_107", "27_127", "27_35", "27_41", "27_56", "27_6", "27_63", "27_67", "27_7", "27_87", "27_95", "28_103", "28_114", "28_125", "28_128", "28_135", "28_137", "28_36", "28_41", "28_7", "28_81", "28_89", "29_113", "29_118", "29_134", "29_139", "29_143", "29_33", "29_35", "30_1", "30_120", "30_121", "30_138", "30_143", "30_53", "30_84", "30_86", "31_1", "31_100", "31_102", "31_114", "31_121", "31_128", "31_144", "31_147", "31_3", "31_32", "31_36", "31_38", "31_39", "31_4", "31_40", "31_41", "31_42", "31_57", "31_6", "31_88", "31_94", "31_99", "32_110", "32_118", "32_122", "32_140", "32_33", "32_37", "32_61", "32_73", "32_81", "32_98", "33_1", "33_120", "33_126", "33_127", "33_129", "33_131", "33_142", "33_144", "33_148", "33_41", "33_5", "33_55", "33_77", "33_8", "33_84", "34_101", "34_110", "34_15", "34_26", "34_29", "34_3", "34_35", "34_37", "34_41", "34_49", "34_6", "34_80", "34_82", "35_111", "35_116", "35_117", "35_125", "35_149", "35_150", "35_18", "35_20", "35_38", "35_43", "35_47", "35_48", "35_49", "35_62", "35_69", "35_72", "35_74", "35_8", "35_80", "35_90", "35_93", "36_106", "36_119", "36_124", "36_126", "36_128", "36_129", "36_132", "36_146", "36_155", "36_25", "36_27", "36_41", "36_43", "36_45", "36_48", "36_49", "36_71", "37_102", "37_107", "37_108", "37_112", "37_119", "37_12", "37_134", "37_142", "37_2", "37_20", "37_23", "37_28", "37_44", "37_45", "37_51", "37_54", "37_68", "37_75", "37_79", "37_9", "38_1", "38_100", "38_106", "38_117", "38_120", "38_122", "38_130", "38_138", "38_157", "38_158", "38_161", "38_25", "38_32", "38_51", "38_55", "38_74", "39_136", "39_141", "39_18", "39_35", "39_5", "39_54", "39_56", "39_58", "39_61", "39_66", "39_90", "39_95", "40_11", "40_155", "40_20", "40_38", "40_47", "40_51", "40_6", "40_81", "41_11", "41_113", "41_124", "41_15", "41_150", "41_157", "41_158", "41_163", "41_165", "41_32", "41_37", "41_55", "41_63", "41_65", "41_67", "41_70", "41_71", "41_92", "42_101", "42_111", "42_132", "42_134", "42_156", "42_158", "42_161", "42_27", "42_33", "42_34", "42_54", "42_77", "42_89", "43_103", "43_113", "43_122", "43_124", "43_13", "43_149", "43_160", "43_166", "43_21", "43_30", "43_35", "43_64", "44_109", "44_12", "44_133", "44_14", "44_158", "44_17", "44_22", "44_29", "44_46", "44_69", "44_72", "44_83", "44_84", "44_96", "45_100", "45_105", "45_118", "45_134", "45_140", "45_150", "45_16", "45_19", "45_31", "45_44", "45_52", "45_9", "45_96", "45_97", "46_1", "46_101", "46_105", "46_114", "46_115", "46_135", "46_151", "46_160", "46_22", "46_30", "46_48", "46_66", "46_99", "47_1", "47_104", "47_108", "47_164", "47_166", "47_21", "47_35", "47_49", "47_61", "47_67", "47_8", "48_1", "48_106", "48_111", "48_121", "48_124", "48_142", "48_144", "48_154", "48_157", "48_166", "48_19", "48_21", "48_33", "48_45", "48_59", "48_62", "48_86", "48_88", "48_99", "49_1", "49_102", "49_13", "49_133", "49_134", "49_139", "49_140", "49_159", "49_16", "49_31", "49_32"};
                for (String clusterId : clusterIds) {
                    for (int i = 0; i <= 10; i++) {
                        ps.setInt(1, IdGenerator.nextId());
                        ps.setInt(2, 1);
                        ps.setString(3, clusterId);
                        if (i == 0)
                            ps.setNull(4, Types.VARCHAR);
                        else
                            ps.setString(4, String.valueOf(i));
                        ps.setString(5, "Location");
                        ps.addBatch();
                    }
                }
                ps.executeBatch();
                ps.close();
            }


            private void taxonomy(Connection connection) throws SQLException {
                connection.createStatement().execute("DELETE FROM ofc_taxonomy");
                PreparedStatement ps = connection.prepareStatement("" +
                        "INSERT INTO ofc_taxonomy (id, survey_id, name, metadata) " +
                        "VALUES (?, 1, ?, ' ')");
                ps.setInt(1, 1);
                ps.setString(2, "trees");
                ps.addBatch();

                ps.setInt(1, 2);
                ps.setString(2, "bamboo");
                ps.addBatch();

                ps.executeBatch();
                ps.close();
            }

            private void taxon(Connection connection) throws SQLException {
                connection.createStatement().execute("DELETE FROM ofc_taxon");
                PreparedStatement ps = connection.prepareStatement("" +
                        "INSERT INTO ofc_taxon (id, taxon_id, code, scientific_name, taxon_rank, taxonomy_id, step, parent_id) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)");

                List data = Arrays.asList(
                        Arrays.asList(1, 1, "ACA", "Acacia sp.", "genus", 1, 9, null),
                        Arrays.asList(2, 2, "ACA/ADE", "Acacia adenocalyx", "species", 1, 9, 1),
                        Arrays.asList(3, 3, "ACA/AUR", "Acacia auriculiformis", "species", 1, 9, 1),
                        Arrays.asList(4, 4, "ACA/BRE", "Acacia brevispica", "species", 1, 9, 1),
                        Arrays.asList(5, 5, "ACA/CLA", "Acacia clavigera", "species", 1, 9, 1),
                        Arrays.asList(6, 6, "ACA/CRA", "Acacia crassicarpa", "species", 1, 9, 1),
                        Arrays.asList(7, 7, "ACA/DRE", "Acacia drepanolobium", "species", 1, 9, 1),
                        Arrays.asList(8, 8, "ACA/ETB", "Acacia etbaica", "species", 1, 9, 1),
                        Arrays.asList(9, 9, "ACA/GER", "Acacia gerrardii", "species", 1, 9, 1),
                        Arrays.asList(10, 10, "ACA/HOC", "Acacia hockii", "species", 1, 9, 1),
                        Arrays.asList(11, 11, "ACA/JUL", "Acacia julifera", "species", 1, 9, 1),
                        Arrays.asList(12, 12, "ACA/LAH", "Acacia lahai", "species", 1, 9, 1),
                        Arrays.asList(13, 13, "ACA/LEP", "Acacia leptocarpa", "species", 1, 9, 1),
                        Arrays.asList(14, 14, "ACA/MAC", "Acacia macrothyrsa", "species", 1, 9, 1),
                        Arrays.asList(15, 15, "ACA/MAN", "Acacia mangium", "species", 1, 9, 1),
                        Arrays.asList(16, 16, "ACA/MEA", "Acacia mearnsii", "species", 1, 9, 1),
                        Arrays.asList(17, 17, "ACA/MEL", "Acacia melanoxylon", "species", 1, 9, 1),
                        Arrays.asList(18, 18, "ACA/MLL", "Acacia mellifera", "species", 1, 9, 1),
                        Arrays.asList(19, 19, "ACA/NIG", "Acacia nigrescens", "species", 1, 9, 1),
                        Arrays.asList(20, 20, "ACA/NIL", "Acacia nilotica", "species", 1, 9, 1),
                        Arrays.asList(21, 21, "ACA/PEN", "Acacia pennata", "species", 1, 9, 1),
                        Arrays.asList(22, 22, "ACA/PNT", "Acacia pentagona", "species", 1, 9, 1),
                        Arrays.asList(23, 23, "ACA/POL", "Acacia polyacantha", "species", 1, 9, 1),
                        Arrays.asList(24, 24, "ACA/ROB", "Acacia robusta", "species", 1, 9, 1),
                        Arrays.asList(25, 25, "ACA/ROV", "Acacia rovumae", "species", 1, 9, 1),
                        Arrays.asList(26, 26, "ACA/SCH", "Acacia schweinfurthii", "species", 1, 9, 1),
                        Arrays.asList(27, 27, "ACA/SEN", "Acacia senegal", "species", 1, 9, 1),
                        Arrays.asList(28, 28, "ACA/SEY", "Acacia seyal", "species", 1, 9, 1),
                        Arrays.asList(29, 29, "ACA/SEY/fistula", "Acacia seyal var. fistula", "subspecies", 1, 9, 28),
                        Arrays.asList(30, 30, "ACA/SEY/seyal", "Acacia seyal var. seyal", "subspecies", 1, 9, 28),
                        Arrays.asList(31, 31, "ACA/SIE", "Acacia sieberiana", "species", 1, 9, 1),
                        Arrays.asList(32, 32, "ACA/STU", "Acacia stuhlmannii", "species", 1, 9, 1),
                        Arrays.asList(33, 33, "ACA/TAN", "Acacia tanganyikensis", "species", 1, 9, 1),
                        Arrays.asList(34, 34, "ACA/TOR", "Acacia tortilis", "species", 1, 9, 1),
                        Arrays.asList(35, 35, "ACA/USA", "Acacia usambarensis", "species", 1, 9, 1),
                        Arrays.asList(36, 36, "ACA/XAN", "Acacia xanthophloea", "species", 1, 9, 1),
                        Arrays.asList(37, 37, "ACL", "Acalypha sp.", "genus", 1, 9, null),
                        Arrays.asList(38, 38, "ACL/BIP", "Acalypha bipartita", "species", 1, 9, 37),
                        Arrays.asList(39, 39, "ACL/CHI", "Acalypha chirindica", "species", 1, 9, 37),
                        Arrays.asList(40, 40, "ACL/ENG", "Acalypha engleri", "species", 1, 9, 37),
                        Arrays.asList(41, 41, "ACL/FRU", "Acalypha fruticosa", "species", 1, 9, 37),
                        Arrays.asList(42, 42, "ACL/GOD", "Acalypha godseffiana", "species", 1, 9, 37),
                        Arrays.asList(43, 43, "ACL/NEP", "Acalypha neptunica", "species", 1, 9, 37),
                        Arrays.asList(44, 44, "ACL/ORN", "Acalypha ornata", "species", 1, 9, 37),
                        Arrays.asList(45, 45, "ACL/WIL", "Acalypha wilkesiana", "species", 1, 9, 37),
                        Arrays.asList(46, 46, "ACT", "Acanthospermum sp.", "genus", 1, 9, null),
                        Arrays.asList(47, 47, "ACN", "Acanthus sp.", "genus", 1, 9, null),
                        Arrays.asList(48, 48, "ACN/PUB", "Acanthus pubescens", "species", 1, 9, 47),
                        Arrays.asList(49, 49, "ACR", "Acrocarpus sp.", "genus", 1, 9, null),
                        Arrays.asList(50, 50, "ACR/FRA", "Acrocarpus fraxinifolius", "species", 1, 9, 49),
                        Arrays.asList(51, 51, "ADA", "Adansonia sp.", "genus", 1, 9, null),
                        Arrays.asList(52, 52, "ADA/DIG", "Adansonia digitata", "species", 1, 9, 51),
                        Arrays.asList(53, 53, "ADN", "Adenia sp.", "genus", 1, 9, null),
                        Arrays.asList(54, 54, "ADN/GLO", "Adenia globosa", "species", 1, 9, 53),
                        Arrays.asList(55, 55, "ADN/MIC", "Adenia microcephala", "species", 1, 9, 53),
                        Arrays.asList(56, 56, "ADE", "Adenium sp.", "genus", 1, 9, null),
                        Arrays.asList(57, 57, "ADE/OBE", "Adenium obesum", "species", 1, 9, 56),
                        Arrays.asList(58, 58, "ADY", "Adyna sp.", "genus", 1, 9, null),
                        Arrays.asList(59, 59, "ADY/ZIM", "Adyna zimmermannii", "species", 1, 9, 58),
                        Arrays.asList(60, 60, "AER", "Aerangis sp.", "genus", 1, 9, null),
                        Arrays.asList(61, 61, "AER/BRA", "Aerangis brachycarpa", "species", 1, 9, 60),
                        Arrays.asList(62, 62, "AER/COR", "Aerangis coriacea", "species", 1, 9, 60),
                        Arrays.asList(63, 63, "AES", "Aeschynomene sp.", "genus", 1, 9, null),
                        Arrays.asList(64, 64, "AES/ABY", "Aeschynomene abyssinica", "species", 1, 9, 63),
                        Arrays.asList(65, 65, "AFR", "Afrocrania sp.", "genus", 1, 9, null),
                        Arrays.asList(66, 66, "AFR/VOL", "Afrocrania volkensii", "species", 1, 9, 65),
                        Arrays.asList(67, 67, "AFS", "Afrosersalicia sp.", "genus", 1, 9, null),
                        Arrays.asList(68, 68, "AFS/CER", "Afrosersalicia cerasifera", "species", 1, 9, 67),
                        Arrays.asList(69, 69, "AFZ", "Afzelia sp.", "genus", 1, 9, null),
                        Arrays.asList(70, 70, "AFZ/QUA", "Afzelia quanzensis", "species", 1, 9, 69),
                        Arrays.asList(71, 71, "AGR", "Agauria sp.", "genus", 1, 9, null),
                        Arrays.asList(72, 72, "AGR/SAL", "Agauria salicifolia", "species", 1, 9, 71),
                        Arrays.asList(73, 73, "AGA", "Agave sp.", "genus", 1, 9, null),
                        Arrays.asList(74, 74, "AGA/SIS", "Agave sisalana", "species", 1, 9, 73),
                        Arrays.asList(75, 75, "AGE", "Agelaea sp.", "genus", 1, 9, null),
                        Arrays.asList(76, 76, "AGE/HET", "Agelaea heterophylla", "species", 1, 9, 75),
                        Arrays.asList(77, 77, "ALF", "Alafia sp.", "genus", 1, 9, null),
                        Arrays.asList(78, 78, "ALF/COR", "Alafia caudata", "species", 1, 9, 77),
                        Arrays.asList(79, 79, "ALA", "Alamanda sp.", "genus", 1, 9, null),
                        Arrays.asList(80, 80, "ALA/VIO", "Alamanda violacea", "species", 1, 9, 79),
                        Arrays.asList(81, 81, "ALN", "Alangium sp.", "genus", 1, 9, null),
                        Arrays.asList(82, 82, "ALN/CHI", "Alangium chinense", "species", 1, 9, 81),
                        Arrays.asList(83, 83, "ALB", "Albizia sp.", "genus", 1, 9, null),
                        Arrays.asList(84, 84, "ALB/ADI", "Albizia adianthifolia", "species", 1, 9, 83),
                        Arrays.asList(85, 85, "ALB/AMA", "Albizia amara", "species", 1, 9, 83),
                        Arrays.asList(86, 86, "ALB/ANT", "Albizia anthelmintica", "species", 1, 9, 83),
                        Arrays.asList(87, 87, "ALB/ANN", "Albizia antunesiana", "species", 1, 9, 83),
                        Arrays.asList(88, 88, "ALB/BRA", "Albizia brachycalyx", "species", 1, 9, 83),
                        Arrays.asList(89, 89, "ALB/CHI", "Albizia chinensis", "species", 1, 9, 83),
                        Arrays.asList(90, 90, "ALB/GLA", "Albizia glaberrima", "species", 1, 9, 83),
                        Arrays.asList(91, 91, "ALB/GLB", "Albizia glabrescens", "species", 1, 9, 83),
                        Arrays.asList(92, 92, "ALB/GUM", "Albizia gummifera", "species", 1, 9, 83),
                        Arrays.asList(93, 93, "ALB/HAR", "Albizia harveyi", "species", 1, 9, 83),
                        Arrays.asList(94, 94, "ALB/LEB", "Albizia lebbeck", "species", 1, 9, 83),
                        Arrays.asList(95, 95, "ALB/PET", "Albizia petersiana", "species", 1, 9, 83),
                        Arrays.asList(96, 96, "ALB/SCH", "Albizia schimperiana", "species", 1, 9, 83),
                        Arrays.asList(97, 97, "ALB/SCH/amaniensis", "Albizia schimperiana var. amaniensis", "subspecies", 1, 9, 96),
                        Arrays.asList(98, 98, "ALB/TAN", "Albizia tanganyikensis", "species", 1, 9, 83),
                        Arrays.asList(99, 99, "ALB/VER", "Albizia versicolor", "species", 1, 9, 83),
                        Arrays.asList(100, 100, "ALB/ZIM", "Albizia zimmermannii", "species", 1, 9, 83),
                        Arrays.asList(101, 101, "ALB/ZYG", "Albizia zygia", "species", 1, 9, 83),
                        Arrays.asList(102, 102, "ALC", "Alchornea sp.", "genus", 1, 9, null),
                        Arrays.asList(103, 103, "ALC/COR", "Alchornea cordifolia", "species", 1, 9, 102),
                        Arrays.asList(104, 104, "ALC/HIR", "Alchornea hirtella", "species", 1, 9, 102),
                        Arrays.asList(105, 105, "ALC/LAX", "Alchornea laxiflora", "species", 1, 9, 102),
                        Arrays.asList(106, 106, "ALE", "Aleurites sp.", "genus", 1, 9, null),
                        Arrays.asList(107, 107, "ALE/MOL", "Aleurites moluccana", "species", 1, 9, 106),
                        Arrays.asList(108, 108, "ALK", "Allanblackia sp.", "genus", 1, 9, null),
                        Arrays.asList(109, 109, "ALK/STU", "Allanblackia stuhlmannii", "species", 1, 9, 108),
                        Arrays.asList(110, 110, "ALK/ULU", "Allanblackia ulugurensis", "species", 1, 9, 108),
                        Arrays.asList(111, 111, "ALL", "Allophylus sp.", "genus", 1, 9, null),
                        Arrays.asList(112, 112, "ALL/ABY", "Allophylus abyssinicus", "species", 1, 9, 111),
                        Arrays.asList(113, 113, "ALL/AFR", "Allophylus africanus", "species", 1, 9, 111),
                        Arrays.asList(114, 114, "ALL/CAL", "Allophylus calophyllus", "species", 1, 9, 111),
                        Arrays.asList(115, 115, "ALL/MAC", "Allophylus macrobotrys", "species", 1, 9, 111),
                        Arrays.asList(116, 116, "ALL/MEL", "Allophylus melliodorus", "species", 1, 9, 111),
                        Arrays.asList(117, 117, "ALL/PSE", "Allophylus pseudopaniculatus", "species", 1, 9, 111),
                        Arrays.asList(118, 118, "ALL/RUB", "Allophylus rubifolius", "species", 1, 9, 111),
                        Arrays.asList(119, 119, "ALL/STA", "Allophylus stachyanthus", "species", 1, 9, 111),
                        Arrays.asList(120, 120, "ALL/VOL", "Allophylus volkensii", "species", 1, 9, 111),
                        Arrays.asList(121, 121, "ALL/ZIM", "Allophylus zimmermannianus", "species", 1, 9, 111),
                        Arrays.asList(122, 122, "ALS", "Alsodeiopsis sp.", "genus", 1, 9, null),
                        Arrays.asList(123, 123, "ALS/DIM", "Alsodeiopsis dimidiata", "species", 1, 9, 122),
                        Arrays.asList(124, 124, "ALS/SCH", "Alsodeiopsis schumannii", "species", 1, 9, 122),
                        Arrays.asList(125, 125, "ALS/USA", "Alsodeiopsis usambarensis", "species", 1, 9, 122),
                        Arrays.asList(126, 126, "ALT", "Alternanthera sp.", "genus", 1, 9, null),
                        Arrays.asList(127, 127, "ALT/SES", "Alternanthera sessilis", "species", 1, 9, 126),
                        Arrays.asList(128, 128, "ALY", "Alysicarpus sp.", "genus", 1, 9, null),
                        Arrays.asList(129, 129, "ALY/RUG", "Alysicarpus rugosum", "species", 1, 9, 128),
                        Arrays.asList(130, 130, "ANA", "Anacardium sp.", "genus", 1, 9, null),
                        Arrays.asList(131, 131, "ANA/OCC", "Anacardium occidentale", "species", 1, 9, 130),
                        Arrays.asList(132, 132, "ANC", "Ancylobothrys sp.", "genus", 1, 9, null),
                        Arrays.asList(133, 133, "ANC/PET", "Ancylobothrys petersiana", "species", 1, 9, 132),
                        Arrays.asList(134, 134, "ANG", "Angylocalyx sp.", "genus", 1, 9, null),
                        Arrays.asList(135, 135, "ANG/BRA", "Angylocalyx braunii", "species", 1, 9, 134),
                        Arrays.asList(136, 136, "ANI", "Aningeria sp.", "genus", 1, 9, null),
                        Arrays.asList(137, 137, "ANI/ADO", "Aningeria adolfi-friedericii", "species", 1, 9, 136),
                        Arrays.asList(138, 138, "ANS", "Anisophyllea sp.", "genus", 1, 9, null),
                        Arrays.asList(139, 139, "ANS/BOE", "Anisophyllea boehmii", "species", 1, 9, 138),
                        Arrays.asList(140, 140, "ANS/OBT", "Anisophyllea obtusifolia", "species", 1, 9, 138),
                        Arrays.asList(141, 141, "ANK", "Annickia sp.", "genus", 1, 9, null),
                        Arrays.asList(142, 142, "ANK/KUM", "Annickia kummeriae", "species", 1, 9, 141),
                        Arrays.asList(143, 143, "ANN", "Annona sp.", "genus", 1, 9, null),
                        Arrays.asList(144, 144, "ANN/CHR", "Annona chrysophylla", "species", 1, 9, 143),
                        Arrays.asList(145, 145, "ANN/MUR", "Annona muricata", "species", 1, 9, 143),
                        Arrays.asList(146, 146, "ANN/SEN", "Annona senegalensis", "species", 1, 9, 143),
                        Arrays.asList(147, 147, "ANN/SQU", "Annona squamosa", "species", 1, 9, 143),
                        Arrays.asList(148, 148, "ANH", "Anthocleista sp.", "genus", 1, 9, null),
                        Arrays.asList(149, 149, "ANH/GRA", "Anthocleista grandiflora", "species", 1, 9, 148),
                        Arrays.asList(150, 150, "ANH/USA", "Anthocleista usambarensis", "species", 1, 9, 148),
                        Arrays.asList(151, 151, "ANR", "Antiaris sp.", "genus", 1, 9, null),
                        Arrays.asList(152, 152, "ANR/TOX", "Antiaris toxicaria", "species", 1, 9, 151),
                        Arrays.asList(153, 153, "ANR/USA", "Antiaris usambarensis", "species", 1, 9, 151),
                        Arrays.asList(154, 154, "ANT", "Antidesma sp.", "genus", 1, 9, null),
                        Arrays.asList(155, 155, "ANT/MEM", "Antidesma membranaceum", "species", 1, 9, 154),
                        Arrays.asList(156, 156, "ANT/VEN", "Antidesma venosum", "species", 1, 9, 154),
                        Arrays.asList(157, 157, "ANP", "Antrophyum sp.", "genus", 1, 9, null),
                        Arrays.asList(158, 158, "ANP/IMM", "Antrophyum immersum", "species", 1, 9, 157),
                        Arrays.asList(159, 159, "AOR", "Aoranthe sp.", "genus", 1, 9, null),
                        Arrays.asList(160, 160, "AOR/PEN", "Aoranthe penduliflora", "species", 1, 9, 159),
                        Arrays.asList(161, 161, "APH", "Aphloia sp.", "genus", 1, 9, null),
                        Arrays.asList(162, 162, "APH/THE", "Aphloia theiformis", "species", 1, 9, 161),
                        Arrays.asList(163, 163, "APO", "Apodostigma sp.", "genus", 1, 9, null),
                        Arrays.asList(164, 164, "APO/PAL", "Apodostigma pallens", "species", 1, 9, 163),
                        Arrays.asList(165, 165, "APD", "Apodytes sp.", "genus", 1, 9, null),
                        Arrays.asList(166, 166, "APD/DIM", "Apodytes dimidiata", "species", 1, 9, 165),
                        Arrays.asList(167, 167, "ARA", "Araucaria sp.", "genus", 1, 9, null),
                        Arrays.asList(168, 168, "ARA/ANG", "Araucaria angustifolia", "species", 1, 9, 167),
                        Arrays.asList(169, 169, "ARA/COL", "Araucaria columnaris", "species", 1, 9, 167),
                        Arrays.asList(170, 170, "ARA/CUN", "Araucaria cunnighamii", "species", 1, 9, 167),
                        Arrays.asList(171, 171, "ARE", "Arenga sp.", "genus", 1, 9, null),
                        Arrays.asList(172, 172, "ARE/PIN", "Arenga pinnata", "species", 1, 9, 171),
                        Arrays.asList(173, 173, "ARG", "Argomuellera sp.", "genus", 1, 9, null),
                        Arrays.asList(174, 174, "ARG/MAC", "Argomuellera macrophylla", "species", 1, 9, 173),
                        Arrays.asList(175, 175, "ARB", "Artabotrys sp.", "genus", 1, 9, null),
                        Arrays.asList(176, 176, "ARB/MOD", "Artabotrys modeiroea", "species", 1, 9, 175),
                        Arrays.asList(177, 177, "ARH", "Arthropteris sp.", "genus", 1, 9, null),
                        Arrays.asList(178, 178, "ARH/MON", "Arthropteris monocarpa", "species", 1, 9, 177),
                        Arrays.asList(179, 179, "ARH/ORI", "Arthropteris orientalis", "species", 1, 9, 177),
                        Arrays.asList(180, 180, "ART", "Artocarpus sp.", "genus", 1, 9, null),
                        Arrays.asList(181, 181, "ART/ALT", "Artocarpus altilis", "species", 1, 9, 180),
                        Arrays.asList(182, 182, "ART/HET", "Artocarpus heterophyllus", "species", 1, 9, 180),
                        Arrays.asList(183, 183, "ASP", "Aspilia sp.", "genus", 1, 9, null),
                        Arrays.asList(184, 184, "ASP/MOS", "Aspilia mossambicensis", "species", 1, 9, 183),
                        Arrays.asList(185, 185, "AST", "Asteranthe sp.", "genus", 1, 9, null),
                        Arrays.asList(186, 186, "AST/AST", "Asteranthe asterias", "species", 1, 9, 185),
                        Arrays.asList(187, 187, "ASY", "Asystasia sp.", "genus", 1, 9, null),
                        Arrays.asList(188, 188, "ASY/SCH", "Asystasia schimperi", "species", 1, 9, 187),
                        Arrays.asList(189, 189, "AUL", "Aulacocalyx sp.", "genus", 1, 9, null),
                        Arrays.asList(190, 190, "AUL/DIE", "Aulacocalyx diervilleoides", "species", 1, 9, 189),
                        Arrays.asList(191, 191, "AVE", "Averrhoa sp.", "genus", 1, 9, null),
                        Arrays.asList(192, 192, "AVE/BIL", "Averrhoa bilimbi", "species", 1, 9, 191),
                        Arrays.asList(193, 193, "AVE/CAR", "Averrhoa carambola", "species", 1, 9, 191),
                        Arrays.asList(194, 194, "AVI", "Avicenia sp.", "genus", 1, 9, null),
                        Arrays.asList(195, 195, "AVI/MAR", "Avicenia marina", "species", 1, 9, 194),
                        Arrays.asList(196, 196, "AZA", "Azadirachta sp.", "genus", 1, 9, null),
                        Arrays.asList(197, 197, "AZA/IND", "Azadirachta indica", "species", 1, 9, 196),
                        Arrays.asList(198, 198, "AZN", "Azanza sp.", "genus", 1, 9, null),
                        Arrays.asList(199, 199, "AZN/GAR", "Azanza garckeana", "species", 1, 9, 198),
                        Arrays.asList(200, 200, "BAI", "Baissea sp.", "genus", 1, 9, null),
                        Arrays.asList(201, 201, "BAI/VIR", "Baissea viridiflora", "species", 1, 9, 200),
                        Arrays.asList(202, 202, "BAL", "Balanites sp.", "genus", 1, 9, null),
                        Arrays.asList(203, 203, "BAL/AEG", "Balanites aegyptiaca", "species", 1, 9, 202),
                        Arrays.asList(204, 204, "BAP", "Baphia sp.", "genus", 1, 9, null),
                        Arrays.asList(205, 205, "BAP/KIR", "Baphia kirkii", "species", 1, 9, 204),
                        Arrays.asList(206, 206, "BPH", "Baphiopsis sp.", "genus", 1, 9, null),
                        Arrays.asList(207, 207, "BPH/STU", "Baphiopsis stuhlmannii", "species", 1, 9, 206),
                        Arrays.asList(208, 208, "BAR", "Barleria sp.", "genus", 1, 9, null),
                        Arrays.asList(209, 209, "BAR/ARG", "Barleria argentea", "species", 1, 9, 208),
                        Arrays.asList(210, 210, "BAR/FUL", "Barleria fulvostellata", "species", 1, 9, 208),
                        Arrays.asList(211, 211, "BRR", "Barringtonia sp.", "genus", 1, 9, null),
                        Arrays.asList(212, 212, "BRR/RAC", "Barringtonia racemosa", "species", 1, 9, 211),
                        Arrays.asList(213, 213, "BAU", "Bauhinia sp.", "genus", 1, 9, null),
                        Arrays.asList(214, 214, "BAU/PET", "Bauhinia petersiana", "species", 1, 9, 213),
                        Arrays.asList(215, 215, "BAU/PUR", "Bauhinia purpurea", "species", 1, 9, 213),
                        Arrays.asList(216, 216, "BEG", "Begonia sp.", "genus", 1, 9, null),
                        Arrays.asList(217, 217, "BEG/MEY", "Begonia meyeri-johannis", "species", 1, 9, 216),
                        Arrays.asList(218, 218, "BEI", "Beilschmiedia sp.", "genus", 1, 9, null),
                        Arrays.asList(219, 219, "BEI/KWE", "Beilschmiedia kweo", "species", 1, 9, 218),
                        Arrays.asList(220, 220, "BEL", "Beloperone sp.", "genus", 1, 9, null),
                        Arrays.asList(221, 221, "BEL/GUT", "Beloperone guttata", "species", 1, 9, 220),
                        Arrays.asList(222, 222, "BLV", "Belvisia sp.", "genus", 1, 9, null),
                        Arrays.asList(223, 223, "BLV/SPI", "Belvisia spicata", "species", 1, 9, 222),
                        Arrays.asList(224, 224, "BEQ", "Bequaertiodendron sp.", "genus", 1, 9, null),
                        Arrays.asList(225, 225, "BEQ/NAT", "Bequaertiodendron natalense", "species", 1, 9, 224),
                        Arrays.asList(226, 226, "BRM", "Berchemia sp.", "genus", 1, 9, null),
                        Arrays.asList(227, 227, "BRM/DIS", "Berchemia discolor", "species", 1, 9, 226),
                        Arrays.asList(228, 228, "BER", "Bersama sp.", "genus", 1, 9, null),
                        Arrays.asList(229, 229, "BER/ABY", "Bersama abyssinica", "species", 1, 9, 228),
                        Arrays.asList(230, 230, "BER/UGA", "Bersama ugandensis", "species", 1, 9, 228),
                        Arrays.asList(231, 231, "BIO", "Biophytum sp.", "genus", 1, 9, null),
                        Arrays.asList(232, 232, "BIO/ABY", "Biophytum abyssinicum", "species", 1, 9, 231),
                        Arrays.asList(233, 233, "BIX", "Bixa sp.", "genus", 1, 9, null),
                        Arrays.asList(234, 234, "BIX/ORE", "Bixa orellana", "species", 1, 9, 233),
                        Arrays.asList(235, 235, "BLI", "Blighia sp.", "genus", 1, 9, null),
                        Arrays.asList(236, 236, "BLI/UNI", "Blighia unijugata", "species", 1, 9, 235),
                        Arrays.asList(237, 237, "BLI/WEL", "Blighia welwitschii", "species", 1, 9, 235),
                        Arrays.asList(238, 238, "BOM", "Bombax sp.", "genus", 1, 9, null),
                        Arrays.asList(239, 239, "BOM/RHO", "Bombax rhodognaphalon", "species", 1, 9, 238),
                        Arrays.asList(240, 240, "BOR", "Borassus sp.", "genus", 1, 9, null),
                        Arrays.asList(241, 241, "BOR/AET", "Borassus aethiopum", "species", 1, 9, 240),
                        Arrays.asList(242, 242, "BSC", "Boscia sp.", "genus", 1, 9, null),
                        Arrays.asList(243, 243, "BSC/ANG", "Boscia angustifolia", "species", 1, 9, 242),
                        Arrays.asList(244, 244, "BSC/COR", "Boscia coriacea", "species", 1, 9, 242),
                        Arrays.asList(245, 245, "BSC/MOS", "Boscia mossambicensis", "species", 1, 9, 242),
                        Arrays.asList(246, 246, "BSC/PAR", "Boscia parviflora", "species", 1, 9, 242),
                        Arrays.asList(247, 247, "BSC/SAL", "Boscia salicifolia", "species", 1, 9, 242),
                        Arrays.asList(248, 248, "BSQ", "Bosqueia sp.", "genus", 1, 9, null),
                        Arrays.asList(249, 249, "BSQ/PHO", "Bosqueia phoberos", "species", 1, 9, 248),
                        Arrays.asList(250, 250, "BOS", "Boswellia sp.", "genus", 1, 9, null),
                        Arrays.asList(251, 251, "BOS/ELE", "Boswellia elegans", "species", 1, 9, 250),
                        Arrays.asList(252, 252, "BRC", "Brachylaena sp.", "genus", 1, 9, null),
                        Arrays.asList(253, 253, "BRC/HUI", "Brachylaena huillensis", "species", 1, 9, 252),
                        Arrays.asList(254, 254, "BRC/HUT", "Brachylaena hutchinsii", "species", 1, 9, 252),
                        Arrays.asList(255, 255, "BRH", "Brachystegia sp.", "genus", 1, 9, null),
                        Arrays.asList(256, 256, "BRH/ALL", "Brachystegia allenii", "species", 1, 9, 255),
                        Arrays.asList(257, 257, "BRH/BOE", "Brachystegia boehmii", "species", 1, 9, 255),
                        Arrays.asList(258, 258, "BRH/BUS", "Brachystegia bussei", "species", 1, 9, 255),
                        Arrays.asList(259, 259, "BRH/FLO", "Brachystegia floribunda", "species", 1, 9, 255),
                        Arrays.asList(260, 260, "BRH/LON", "Brachystegia longifolia", "species", 1, 9, 255),
                        Arrays.asList(261, 261, "BRH/MIC", "Brachystegia microphylla", "species", 1, 9, 255),
                        Arrays.asList(262, 262, "BRH/SPI", "Brachystegia spiciformis", "species", 1, 9, 255),
                        Arrays.asList(263, 263, "BRH/TAM", "Brachystegia tamarindoides", "species", 1, 9, 255),
                        Arrays.asList(264, 264, "BRH/UTI", "Brachystegia utilis", "species", 1, 9, 255),
                        Arrays.asList(265, 265, "BRH/WAN", "Brachystegia wangermeeana", "species", 1, 9, 255),
                        Arrays.asList(266, 266, "BRS", "Brachystephanus sp.", "genus", 1, 9, null),
                        Arrays.asList(267, 267, "BRS/AFR", "Brachystephanus africanus", "species", 1, 9, 266),
                        Arrays.asList(268, 268, "BRS/HOL", "Brachystephanus holstii", "species", 1, 9, 266),
                        Arrays.asList(269, 269, "BRK", "Brackenridgea sp.", "genus", 1, 9, null),
                        Arrays.asList(270, 270, "BRK/BUS", "Brackenridgea bussei", "species", 1, 9, 269),
                        Arrays.asList(271, 271, "BRK/ZAN", "Brackenridgea zanguebarica", "species", 1, 9, 269),
                        Arrays.asList(272, 272, "BRA", "Brassica sp.", "genus", 1, 9, null),
                        Arrays.asList(273, 273, "BRA/OLE", "Brassica oleracea", "species", 1, 9, 272),
                        Arrays.asList(274, 274, "BRE", "Breonadia sp.", "genus", 1, 9, null),
                        Arrays.asList(275, 275, "BRE/SAL", "Breonadia salicina", "species", 1, 9, 274),
                        Arrays.asList(276, 276, "BRI", "Bridelia sp.", "genus", 1, 9, null),
                        Arrays.asList(277, 277, "BRI/BRI", "Bridelia brideliifolia", "species", 1, 9, 276),
                        Arrays.asList(278, 278, "BRI/CAT", "Bridelia cathartica", "species", 1, 9, 276),
                        Arrays.asList(279, 279, "BRI/DUV", "Bridelia duvigneaudii", "species", 1, 9, 276),
                        Arrays.asList(280, 280, "BRI/MEL", "Bridelia melanthesoides", "species", 1, 9, 276),
                        Arrays.asList(281, 281, "BRI/MIC", "Bridelia micrantha", "species", 1, 9, 276),
                        Arrays.asList(282, 282, "BRI/SAL", "Bridelia salviifolia", "species", 1, 9, 276),
                        Arrays.asList(283, 283, "BRI/SCL", "Bridelia scleroneura", "species", 1, 9, 276),
                        Arrays.asList(284, 284, "BRU", "Brucea sp.", "genus", 1, 9, null),
                        Arrays.asList(285, 285, "BRU/ANT", "Brucea antidysenterica", "species", 1, 9, 284),
                        Arrays.asList(286, 286, "BRU/TEN", "Brucea tenuifolia", "species", 1, 9, 284),
                        Arrays.asList(287, 287, "BRG", "Brugueira sp.", "genus", 1, 9, null),
                        Arrays.asList(288, 288, "BRG/GYM", "Brugueira gymnorhiza", "species", 1, 9, 287),
                        Arrays.asList(289, 289, "BRY", "Bryophyllum sp.", "genus", 1, 9, null),
                        Arrays.asList(290, 290, "BRY/PIN", "Bryophyllum pinnatum", "species", 1, 9, 289),
                        Arrays.asList(291, 291, "BUR", "Burkea sp.", "genus", 1, 9, null),
                        Arrays.asList(292, 292, "BUR/AFR", "Burkea africana", "species", 1, 9, 291),
                        Arrays.asList(293, 293, "BRT", "Burttia sp.", "genus", 1, 9, null),
                        Arrays.asList(294, 294, "BRT/PRU", "Burttia prunoides", "species", 1, 9, 293),
                        Arrays.asList(295, 295, "BUS", "Bussea sp.", "genus", 1, 9, null),
                        Arrays.asList(296, 296, "BUS/MAS", "Bussea massaiensis", "species", 1, 9, 295),
                        Arrays.asList(297, 297, "BYR", "Byrsocarpus sp.", "genus", 1, 9, null),
                        Arrays.asList(298, 298, "BYR/BOI", "Byrsocarpus boivinianus", "species", 1, 9, 297),
                        Arrays.asList(299, 299, "BYR/ORI", "Byrsocarpus orientalis", "species", 1, 9, 297),
                        Arrays.asList(300, 300, "CAE", "Caesalpinia sp.", "genus", 1, 9, null),
                        Arrays.asList(301, 301, "CAE/DEC", "Caesalpinia decapetala", "species", 1, 9, 300),
                        Arrays.asList(302, 302, "CAL", "Callistemon sp.", "genus", 1, 9, null),
                        Arrays.asList(303, 303, "CAL/SPE", "Callistemon speciosus", "species", 1, 9, 302),
                        Arrays.asList(304, 304, "CLD", "Calodendron sp.", "genus", 1, 9, null),
                        Arrays.asList(305, 305, "CLD/CAP", "Calodendron capense", "species", 1, 9, 304),
                        Arrays.asList(306, 306, "CLB", "Caloncoba sp.", "genus", 1, 9, null),
                        Arrays.asList(307, 307, "CLB/WEL", "Caloncoba welwitschii", "species", 1, 9, 306),
                        Arrays.asList(308, 308, "CLT", "Calotropis sp.", "genus", 1, 9, null),
                        Arrays.asList(309, 309, "CLT/PRO", "Calotropis procera", "species", 1, 9, 308),
                        Arrays.asList(310, 310, "CAM", "Camptopus sp.", "genus", 1, 9, null),
                        Arrays.asList(311, 311, "CAM/GOE", "Camptopus goetzei", "species", 1, 9, 310),
                        Arrays.asList(312, 312, "CAN", "Cananga sp.", "genus", 1, 9, null),
                        Arrays.asList(313, 313, "CAN/ODO", "Cananga odorata", "species", 1, 9, 312),
                        Arrays.asList(314, 314, "CNR", "Canarium sp.", "genus", 1, 9, null),
                        Arrays.asList(315, 315, "CNR/SCH", "Canarium schweinfurthii", "species", 1, 9, 314),
                        Arrays.asList(316, 316, "CNT", "Canthium sp.", "genus", 1, 9, null),
                        Arrays.asList(317, 317, "CNT/BUR", "Canthium burtii", "species", 1, 9, 316),
                        Arrays.asList(318, 318, "CNT/CAP", "Canthium captum", "species", 1, 9, 316),
                        Arrays.asList(319, 319, "CNT/CRA", "Canthium crassum", "species", 1, 9, 316),
                        Arrays.asList(320, 320, "CNT/GUE", "Canthium gueinzii", "species", 1, 9, 316),
                        Arrays.asList(321, 321, "CNT/HIS", "Canthium hispidum", "species", 1, 9, 316),
                        Arrays.asList(322, 322, "CNT/HUI", "Canthium huillense", "species", 1, 9, 316),
                        Arrays.asList(323, 323, "CNT/MOM", "Canthium mombasense", "species", 1, 9, 316),
                        Arrays.asList(324, 324, "CNT/OLI", "Canthium oligocarpum", "species", 1, 9, 316),
                        Arrays.asList(325, 325, "CNT/PAR", "Canthium parviflora", "species", 1, 9, 316),
                        Arrays.asList(326, 326, "CAP", "Capparis sp.", "genus", 1, 9, null),
                        Arrays.asList(327, 327, "CAP/SEP", "Capparis sepiaria", "species", 1, 9, 326),
                        Arrays.asList(328, 328, "CAP/TOM", "Capparis tomentosa", "species", 1, 9, 326),
                        Arrays.asList(329, 329, "CRD", "Cardiospermum sp.", "genus", 1, 9, null),
                        Arrays.asList(330, 330, "CRD/HAL", "Cardiospermum halicacabum", "species", 1, 9, 329),
                        Arrays.asList(331, 331, "CAR", "Carica sp.", "genus", 1, 9, null),
                        Arrays.asList(332, 332, "CAR/PAP", "Carica papaya", "species", 1, 9, 331),
                        Arrays.asList(333, 333, "CRS", "Carissa sp.", "genus", 1, 9, null),
                        Arrays.asList(334, 334, "CRS/EDU", "Carissa edulis", "species", 1, 9, 333),
                        Arrays.asList(335, 335, "CRS/MAC", "Carissa macrocarpa", "species", 1, 9, 333),
                        Arrays.asList(336, 336, "CRR", "Carpodiptera sp.", "genus", 1, 9, null),
                        Arrays.asList(337, 337, "CRR/AFR", "Carpodiptera africana", "species", 1, 9, 336),
                        Arrays.asList(338, 338, "CAY", "Caryota sp.", "genus", 1, 9, null),
                        Arrays.asList(339, 339, "CAY/URE", "Caryota urens", "species", 1, 9, 338),
                        Arrays.asList(340, 340, "CAS", "Casearia sp.", "genus", 1, 9, null),
                        Arrays.asList(341, 341, "CAS/BAT", "Casearia battiscombei", "species", 1, 9, 340),
                        Arrays.asList(342, 342, "CAS/GLA", "Casearia gladiiformis", "species", 1, 9, 340),
                        Arrays.asList(343, 343, "CSS", "Cassia sp.", "genus", 1, 9, null),
                        Arrays.asList(344, 344, "CSS/ABB", "Cassia abbreviata", "species", 1, 9, 343),
                        Arrays.asList(345, 345, "CSS/ACC", "Cassia accidentalis", "species", 1, 9, 343),
                        Arrays.asList(346, 346, "CSS/ANG", "Cassia angolensis", "species", 1, 9, 343),
                        Arrays.asList(347, 347, "CSS/DID", "Cassia didymobotrya", "species", 1, 9, 343),
                        Arrays.asList(348, 348, "CSS/FIS", "Cassia fistula", "species", 1, 9, 343),
                        Arrays.asList(349, 349, "CSS/MIM", "Cassia mimosoides", "species", 1, 9, 343),
                        Arrays.asList(350, 350, "CSP", "Cassipourea sp.", "genus", 1, 9, null),
                        Arrays.asList(351, 351, "CSP/GUM", "Cassipourea gummiflua", "species", 1, 9, 350),
                        Arrays.asList(352, 352, "CSP/MAL", "Cassipourea malosana", "species", 1, 9, 350),
                        Arrays.asList(353, 353, "CSP/MOL", "Cassipourea mollis", "species", 1, 9, 350),
                        Arrays.asList(354, 354, "CST", "Castilla sp.", "genus", 1, 9, null),
                        Arrays.asList(355, 355, "CST/ELA", "Castilla elastica", "species", 1, 9, 354),
                        Arrays.asList(356, 356, "CSR", "Casuarina sp.", "genus", 1, 9, null),
                        Arrays.asList(357, 357, "CSR/EQU", "Casuarina equisetifolia", "species", 1, 9, 356),
                        Arrays.asList(358, 358, "CSR/GLA", "Casuarina glauca", "species", 1, 9, 356),
                        Arrays.asList(359, 359, "CSR/JUN", "Casuarina junghuhniana", "species", 1, 9, 356),
                        Arrays.asList(360, 360, "CSR/LIT", "Casuarina littoralis", "species", 1, 9, 356),
                        Arrays.asList(361, 361, "CSR/OBE", "Casuarina obesa", "species", 1, 9, 356),
                        Arrays.asList(362, 362, "CAT", "Catha sp.", "genus", 1, 9, null),
                        Arrays.asList(363, 363, "CAT/EDU", "Catha edulis", "species", 1, 9, 362),
                        Arrays.asList(364, 364, "CTN", "Catunaregam sp.", "genus", 1, 9, null),
                        Arrays.asList(365, 365, "CTN/SPI", "Catunaregam spinosa", "species", 1, 9, 364),
                        Arrays.asList(366, 366, "CED", "Cedrella sp.", "genus", 1, 9, null),
                        Arrays.asList(367, 367, "CED/ODO", "Cedrella odorata", "species", 1, 9, 366),
                        Arrays.asList(368, 368, "CEI", "Ceiba sp.", "genus", 1, 9, null),
                        Arrays.asList(369, 369, "CEI/PEN", "Ceiba pentandra", "species", 1, 9, 368),
                        Arrays.asList(370, 370, "CLS", "Celastraceae sp.", "genus", 1, 9, null),
                        Arrays.asList(371, 371, "CLS/CAS", "Celastraceae cassine", "species", 1, 9, 370),
                        Arrays.asList(372, 372, "CEL", "Celtis sp.", "genus", 1, 9, null),
                        Arrays.asList(373, 373, "CEL/AFR", "Celtis africana", "species", 1, 9, 372),
                        Arrays.asList(374, 374, "CEL/DUR", "Celtis durandii", "species", 1, 9, 372),
                        Arrays.asList(375, 375, "CEL/GER", "Celtis gerrardii", "species", 1, 9, 372),
                        Arrays.asList(376, 376, "CEL/GOM", "Celtis gomphophylla", "species", 1, 9, 372),
                        Arrays.asList(377, 377, "CEL/KRA", "Celtis kraussia", "species", 1, 9, 372),
                        Arrays.asList(378, 378, "CEL/MIL", "Celtis mildbraedii", "species", 1, 9, 372),
                        Arrays.asList(379, 379, "CEL/PHI", "Celtis philippensis", "species", 1, 9, 372),
                        Arrays.asList(380, 380, "CEL/WIG", "Celtis wightii", "species", 1, 9, 372),
                        Arrays.asList(381, 381, "CEL/ZEN", "Celtis zenkeri", "species", 1, 9, 372),
                        Arrays.asList(382, 382, "CEP", "Cephalosphaera sp.", "genus", 1, 9, null),
                        Arrays.asList(383, 383, "CEP/USA", "Cephalosphaera usambarensis", "species", 1, 9, 382),
                        Arrays.asList(384, 384, "CER", "Ceratophyllum sp.", "genus", 1, 9, null),
                        Arrays.asList(385, 385, "CER/DEM", "Ceratophyllum demersum", "species", 1, 9, 384),
                        Arrays.asList(386, 386, "CRH", "Ceratotheca sp.", "genus", 1, 9, null),
                        Arrays.asList(387, 387, "CRH/SES", "Ceratotheca sesamoides", "species", 1, 9, 386),
                        Arrays.asList(388, 388, "CPS", "Ceriops sp.", "genus", 1, 9, null),
                        Arrays.asList(389, 389, "CPS/TAG", "Ceriops tagal", "species", 1, 9, 388),
                        Arrays.asList(390, 390, "CHT", "Chaetacme sp.", "genus", 1, 9, null),
                        Arrays.asList(391, 391, "CHT/ARI", "Chaetacme aristata", "species", 1, 9, 390),
                        Arrays.asList(392, 392, "CHM", "Chamaecrista sp.", "genus", 1, 9, null),
                        Arrays.asList(393, 393, "CHM/ABS", "Chamaecrista absus", "species", 1, 9, 392),
                        Arrays.asList(394, 394, "CHA", "Chamaecyparis sp.", "genus", 1, 9, null),
                        Arrays.asList(395, 395, "CHA/LAW", "Chamaecyparis lawsoniana", "species", 1, 9, 394),
                        Arrays.asList(396, 396, "CHS", "Chassalia sp.", "genus", 1, 9, null),
                        Arrays.asList(397, 397, "CHS/ALB", "Chassalia albiflora", "species", 1, 9, 396),
                        Arrays.asList(398, 398, "CHI", "Chionanthus sp.", "genus", 1, 9, null),
                        Arrays.asList(399, 399, "CHI/AFR", "Chionanthus africana", "species", 1, 9, 398),
                        Arrays.asList(400, 400, "CHI/BAT", "Chionanthus battiscombei", "species", 1, 9, 398),
                        Arrays.asList(401, 401, "CHL", "Chlorophytum sp.", "genus", 1, 9, null),
                        Arrays.asList(402, 402, "CHL/SPA", "Chlorophytum sparsiflorum", "species", 1, 9, 401),
                        Arrays.asList(403, 403, "CHD", "Chrysalidocarpus sp.", "genus", 1, 9, null),
                        Arrays.asList(404, 404, "CHD/LUT", "Chrysalidocarpus lutescens", "species", 1, 9, 403),
                        Arrays.asList(405, 405, "CHR", "Chrysanthemoides sp.", "genus", 1, 9, null),
                        Arrays.asList(406, 406, "CHR/MON", "Chrysanthemoides monilifera", "species", 1, 9, 405),
                        Arrays.asList(407, 407, "CHP", "Chrysophyllum sp.", "genus", 1, 9, null),
                        Arrays.asList(408, 408, "CHP/BAN", "Chrysophyllum bangweolense", "species", 1, 9, 407),
                        Arrays.asList(409, 409, "CHP/GOR", "Chrysophyllum gorungosanum", "species", 1, 9, 407),
                        Arrays.asList(410, 410, "CHP/PER", "Chrysophyllum perpulchrum", "species", 1, 9, 407),
                        Arrays.asList(411, 411, "CHP/ZIM", "Chrysophyllum zimmermannii", "species", 1, 9, 407),
                        Arrays.asList(412, 412, "CHY", "Chytranthus sp.", "genus", 1, 9, null),
                        Arrays.asList(413, 413, "CHY/OBL", "Chytranthus obliquinervis", "species", 1, 9, 412),
                        Arrays.asList(414, 414, "CIN", "Cinchona sp.", "genus", 1, 9, null),
                        Arrays.asList(415, 415, "CIN/LED", "Cinchona ledgeriana", "species", 1, 9, 414),
                        Arrays.asList(416, 416, "CNN", "Cinnamomum sp.", "genus", 1, 9, null),
                        Arrays.asList(417, 417, "CNN/CAM", "Cinnamomum camphora", "species", 1, 9, 416),
                        Arrays.asList(418, 418, "CNN/ZEI", "Cinnamomum zeilanicum", "species", 1, 9, 416),
                        Arrays.asList(419, 419, "CIT", "Citrus sp.", "genus", 1, 9, null),
                        Arrays.asList(420, 420, "CIT/ARN", "Citrus aurantifolia", "species", 1, 9, 419),
                        Arrays.asList(421, 421, "CIT/AUR", "Citrus aurantium", "species", 1, 9, 419),
                        Arrays.asList(422, 422, "CIT/LIM", "Citrus limonia", "species", 1, 9, 419),
                        Arrays.asList(423, 423, "CIT/SIN", "Citrus sinensis", "species", 1, 9, 419),
                        Arrays.asList(424, 424, "CLA", "Clausena sp.", "genus", 1, 9, null),
                        Arrays.asList(425, 425, "CLA/ANI", "Clausena anisata", "species", 1, 9, 424),
                        Arrays.asList(426, 426, "CLN", "Cleistanthus sp.", "genus", 1, 9, null),
                        Arrays.asList(427, 427, "CLN/AMA", "Cleistanthus amaniensis", "species", 1, 9, 426),
                        Arrays.asList(428, 428, "CLN/POL", "Cleistanthus polystachyus", "species", 1, 9, 426),
                        Arrays.asList(429, 429, "CLH", "Cleistochlamys sp.", "genus", 1, 9, null),
                        Arrays.asList(430, 430, "CLH/KIR", "Cleistochlamys kirkii", "species", 1, 9, 429),
                        Arrays.asList(431, 431, "CLE", "Clematis sp.", "genus", 1, 9, null),
                        Arrays.asList(432, 432, "CLE/BRA", "Clematis brachiata", "species", 1, 9, 431),
                        Arrays.asList(433, 433, "CLE/SIN", "Clematis sinensis", "species", 1, 9, 431),
                        Arrays.asList(434, 434, "CLM", "Cleome sp.", "genus", 1, 9, null),
                        Arrays.asList(435, 435, "CLM/HIR", "Cleome hirta", "species", 1, 9, 434),
                        Arrays.asList(436, 436, "CLM/MON", "Cleome monophylla", "species", 1, 9, 434),
                        Arrays.asList(2052, 2052, "ZNT/BRA", "Zanthoxylum braunii", "species", 1, 9, 2051),
                        Arrays.asList(2053, 2053, "ZNT/CHA", "Zanthoxylum chalybeum", "species", 1, 9, 2051),
                        Arrays.asList(2054, 2054, "ZNT/DER", "Zanthoxylum deremense", "species", 1, 9, 2051),
                        Arrays.asList(2055, 2055, "ZNT/GIL", "Zanthoxylum gillettii", "species", 1, 9, 2051),
                        Arrays.asList(2056, 2056, "ZNT/HOL", "Zanthoxylum holtzianum", "species", 1, 9, 2051),
                        Arrays.asList(2057, 2057, "ZNT/MAR", "Zanthoxylum markeri", "species", 1, 9, 2051),
                        Arrays.asList(2058, 2058, "ZNT/MIL", "Zanthoxylum mildbraedii", "species", 1, 9, 2051),
                        Arrays.asList(2059, 2059, "ZNT/OLI", "Zanthoxylum olitoriam", "species", 1, 9, 2051),
                        Arrays.asList(2060, 2060, "ZNT/USA", "Zanthoxylum usambarense", "species", 1, 9, 2051),
                        Arrays.asList(2061, 2061, "ZEN", "Zenkerella sp.", "genus", 1, 9, null),
                        Arrays.asList(2062, 2062, "ZEN/EGR", "Zenkerella egregia", "species", 1, 9, 2061),
                        Arrays.asList(2063, 2063, "ZEN/GRO", "Zenkerella grotei", "species", 1, 9, 2061),
                        Arrays.asList(2064, 2064, "ZIZ", "Ziziphus sp.", "genus", 1, 9, null),
                        Arrays.asList(2065, 2065, "ZIZ/ABY", "Ziziphus abyssinica", "species", 1, 9, 2064),
                        Arrays.asList(2066, 2066, "ZIZ/MAU", "Ziziphus mauritiana", "species", 1, 9, 2064),
                        Arrays.asList(2067, 2067, "ZIZ/MUC", "Ziziphus mucronata", "species", 1, 9, 2064),
                        Arrays.asList(2068, 2068, "ZIZ/PUB", "Ziziphus pubescens", "species", 1, 9, 2064),
                        Arrays.asList(2069, 2069, "CON", "Cornus sp.", "genus", 1, 9, null),
                        Arrays.asList(2070, 2070, "CON/VOL", "Cornus volkensii", "species", 1, 9, 2069),
                        Arrays.asList(2071, 2071, "AGS", "Agarista sp.", "genus", 1, 9, null),
                        Arrays.asList(2072, 2072, "HEN", "Heinsenia sp.", "genus", 1, 9, null),
                        Arrays.asList(2073, 2073, "HEN/DIE", "Heinsenia diervilleoides", "species", 1, 9, 2072),
                        Arrays.asList(2074, 2074, "HYE", "Hymenaea sp.", "genus", 1, 9, null),
                        Arrays.asList(2075, 2075, "HYE/VER", "Hymenaea verrucosa", "species", 1, 9, 2074),
                        Arrays.asList(2076, 2076, "TES", "Tessmannia sp.", "genus", 1, 9, null),
                        Arrays.asList(2077, 2077, "TES/MAR", "Tessmannia martiniana", "species", 1, 9, 2076),
                        Arrays.asList(2078, 2078, "HPL", "Haplocoelopsis sp.", "genus", 1, 9, null),
                        Arrays.asList(2079, 2079, "HPL/AFR", "Haplocoelopsis africana", "species", 1, 9, 2078),
                        Arrays.asList(2080, 2080, "LIN", "Lindackeria sp.", "genus", 1, 9, null),
                        Arrays.asList(2081, 2081, "LIN/BUK", "Lindackeria bukobensis", "species", 1, 9, 2080),
                        Arrays.asList(2082, 2082, "LIN/STI", "Lindackeria stipulata", "species", 1, 9, 2080),
                        Arrays.asList(2083, 2083, "GLI", "Gliricidia sp.", "genus", 1, 9, null),
                        Arrays.asList(2084, 2084, "GLI/SEP", "Gliricidia sepium", "species", 1, 9, 2083),
                        Arrays.asList(2085, 2085, "DPH", "Diphasia sp.", "genus", 1, 9, null),
                        Arrays.asList(2086, 2086, "DPH/MOR", "Diphasia morogorensis", "species", 1, 9, 2085),
                        Arrays.asList(2087, 2087, "MAM", "Mammea sp.", "genus", 1, 9, null),
                        Arrays.asList(2088, 2088, "MAM/USA", "Mammea usambarensis", "species", 1, 9, 2087),
                        Arrays.asList(2089, 2089, "AFC", "Afrocanthium sp.", "genus", 1, 9, null),
                        Arrays.asList(2090, 2090, "AFC/LAC", "Afrocanthium lactescens", "species", 1, 9, 2089),
                        Arrays.asList(2091, 2091, "PHC", "Phyllocosmus sp.", "genus", 1, 9, null),
                        Arrays.asList(2092, 2092, "PHC/LEM", "Phyllocosmus lemaireanus", "species", 1, 9, 2091),
                        Arrays.asList(2093, 2093, "AZI", "Azima sp.", "genus", 1, 9, null),
                        Arrays.asList(2094, 2094, "AZI/TET", "Azima tetracantha", "species", 1, 9, 2093),
                        Arrays.asList(2095, 2095, "PYN", "Pynocoma sp.", "genus", 1, 9, null),
                        Arrays.asList(2096, 2096, "PYN/MAC", "Pynocoma macrantha", "species", 1, 9, 2095),
                        Arrays.asList(2097, 2097, "LSD", "Lasiodiscus sp.", "genus", 1, 9, null),
                        Arrays.asList(2098, 2098, "LSD/MIL", "Lasiodiscus mildbraedii", "species", 1, 9, 2097),
                        Arrays.asList(2099, 2099, "LSD/USA", "Lasiodiscus usambarensis", "species", 1, 9, 2097),
                        Arrays.asList(2100, 2100, "CLP", "Calycosphonia sp.", "genus", 1, 9, null),
                        Arrays.asList(2101, 2101, "CLP/SPA", "Calycosphonia spathicalix", "species", 1, 9, 2100),
                        Arrays.asList(2102, 2102, "ARC", "Areca sp.", "genus", 1, 9, null),
                        Arrays.asList(2103, 2103, "ARC/CAT", "Areca catechu", "species", 1, 9, 2102),
                        Arrays.asList(2104, 1, "ARU", "Arundinaria sp.", "genus", 2, 9, null),
                        Arrays.asList(2105, 2, "ARU/ALP", "Arundinaria alpina", "species", 2, 9, 2104),
                        Arrays.asList(2106, 3, "ARU/TOL", "Arundinaria tolange", "species", 2, 9, 2104),
                        Arrays.asList(2107, 4, "BAM", "Bambusa sp.", "genus", 2, 9, null),
                        Arrays.asList(2108, 5, "BAM/BAM", "Bambusa bambos", "species", 2, 9, 2107),
                        Arrays.asList(2109, 6, "BAM/MUL", "Bambusa multiplex", "species", 2, 9, 2107),
                        Arrays.asList(2110, 7, "BAM/NUT", "Bambusa nutans", "species", 2, 9, 2107),
                        Arrays.asList(2111, 8, "BAM/TEX", "Bambusa textilis", "species", 2, 9, 2107),
                        Arrays.asList(2112, 9, "BAM/VUL", "Bambusa vulgaris", "species", 2, 9, 2107),
                        Arrays.asList(2113, 10, "CHN", "Chimono-bambusa sp.", "genus", 2, 9, null),
                        Arrays.asList(2114, 11, "CHN/HOO", "Chimono-bambusa hookeriana", "species", 2, 9, 2113),
                        Arrays.asList(2115, 12, "ORE", "Oreobambos sp.", "genus", 2, 9, null),
                        Arrays.asList(2116, 13, "ORE/BUC", "Oreobambos buchwaldii", "species", 2, 9, 2115),
                        Arrays.asList(2117, 14, "OXT", "Oxytenanthera sp.", "genus", 2, 9, null),
                        Arrays.asList(2118, 15, "OXT/ABY", "Oxytenanthera abyssinica", "species", 2, 9, 2117),
                        Arrays.asList(2119, 16, "OXT/BRA", "Oxytenanthera braunii", "species", 2, 9, 2117)
                );
                for (Object o : data) {
                    List taxon = (List) o;
                    ps.setInt(1, (Integer) taxon.get(0));
                    ps.setInt(2, (Integer) taxon.get(1));
                    ps.setString(3, (String) taxon.get(2));
                    ps.setString(4, (String) taxon.get(3));
                    ps.setString(5, (String) taxon.get(4));
                    ps.setInt(6, (Integer) taxon.get(5));
                    ps.setInt(7, (Integer) taxon.get(6));
                    Integer parentId = (Integer) taxon.get(7);
                    if (parentId == null)
                        ps.setNull(8, Types.INTEGER);
                    else
                        ps.setInt(8, parentId);
                    ps.addBatch();
                }

                ps.executeBatch();
                ps.close();
            }
        });
    }


    private void close(liquibase.database.Database database) {
        try {
            if (database != null)
                database.close();
        } catch (DatabaseException e) {
            throw new IllegalStateException("Failed to close liquibase database", e);
        }
    }

}

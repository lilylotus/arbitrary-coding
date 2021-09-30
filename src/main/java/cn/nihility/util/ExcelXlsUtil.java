package cn.nihility.util;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.util.CellRangeAddress;

public class ExcelXlsUtil {

    /**
     * 复制行
     *
     * @param startCol  起始列
     * @param endCol    结束列
     * @param pPosition 目标起始行位置
     */
    public static HSSFSheet copyCols(HSSFSheet currentSheet, int startCol, int endCol, int pPosition) {
        int pStartCol = startCol - 1;
        int pEndCol = endCol - 1;
        int targetColFrom;
        int targetColTo;
        int RowCount;
        CellRangeAddress region = null;
        int i;
        int j;
        if (pStartCol == -1 || pEndCol == -1) {
            return null;
        }
        for (i = 0; i < currentSheet.getNumMergedRegions(); i++) {
            region = currentSheet.getMergedRegion(i);
            if ((region.getFirstColumn() >= pStartCol)
                && (region.getLastColumn() <= pEndCol)) {
                targetColFrom = region.getFirstColumn() - pStartCol + pPosition;
                targetColTo = region.getLastColumn() - pStartCol + pPosition;
                CellRangeAddress newRegion = region.copy();
                newRegion.setFirstRow(region.getFirstRow());
                newRegion.setFirstColumn(targetColFrom);
                newRegion.setLastRow(region.getLastRow());
                newRegion.setLastColumn(targetColTo);
                currentSheet.addMergedRegion(newRegion);

            }
        }
        for (i = 0; i <= 50; i++) {
            HSSFRow sourceRow = currentSheet.getRow(i);
            if (sourceRow != null) {
                HSSFRow newRow = currentSheet.getRow(i);
                for (j = 0; j < pEndCol; j++) {
                    HSSFCell templateCell = sourceRow.getCell(j);
                    if (i == 0) {
                        currentSheet.setColumnWidth(pPosition + j, currentSheet.getColumnWidth(j));
                    }
                    if (templateCell != null) {
                        HSSFCell newCell = newRow.createCell(pPosition + j);
                        copyCell(templateCell, newCell);
                    }
                }
            }
        }
        return currentSheet;
    }

    public static void copyRows(HSSFSheet currentSheet, int startRow, int endRow, int pPosition) {
        int pStartRow = startRow - 1;
        int pEndRow = endRow - 1;
        int targetRowFrom;
        int targetRowTo;
        int columnCount;
        CellRangeAddress region = null;
        int i;
        int j;
        if (pStartRow == -1 || pEndRow == -1) {
            return;
        }
        for (i = 0; i < currentSheet.getNumMergedRegions(); i++) {
            region = currentSheet.getMergedRegion(i);
            if ((region.getFirstRow() >= pStartRow)
                && (region.getLastRow() <= pEndRow)) {
                targetRowFrom = region.getFirstRow() - pStartRow + pPosition;
                targetRowTo = region.getLastRow() - pStartRow + pPosition;
                CellRangeAddress newRegion = region.copy();
                newRegion.setFirstRow(targetRowFrom);
                newRegion.setFirstColumn(region.getFirstColumn());
                newRegion.setLastRow(targetRowTo);
                newRegion.setLastColumn(region.getLastColumn());
                currentSheet.addMergedRegion(newRegion);
            }
        }
        for (i = pStartRow; i <= pEndRow; i++) {
            HSSFRow sourceRow = currentSheet.getRow(i);
            columnCount = sourceRow.getLastCellNum();
            if (sourceRow != null) {
                HSSFRow newRow = currentSheet.createRow(pPosition - pStartRow
                    + i);
                newRow.setHeight(sourceRow.getHeight());
                for (j = 0; j < columnCount; j++) {
                    HSSFCell templateCell = sourceRow.getCell(j);
                    if (templateCell != null) {
                        HSSFCell newCell = newRow.createCell(j);
                        copyCell(templateCell, newCell);
                    }
                }
            }
        }
    }

    private static void copyCell(HSSFCell srcCell, HSSFCell distCell) {
        distCell.setCellStyle(srcCell.getCellStyle());
        if (srcCell.getCellComment() != null) {
            distCell.setCellComment(srcCell.getCellComment());
        }
        CellType srcCellType = srcCell.getCellType();
        distCell.setCellType(srcCellType);
        if (srcCellType == CellType.NUMERIC) {
            if (DateUtil.isCellDateFormatted(srcCell)) {
                distCell.setCellValue(srcCell.getDateCellValue());
            } else {
                distCell.setCellValue(srcCell.getNumericCellValue());
            }
        } else if (srcCellType == CellType.STRING) {
            distCell.setCellValue(srcCell.getRichStringCellValue());
        } else if (srcCellType == CellType.BLANK) {
            // nothing21
        } else if (srcCellType == CellType.BOOLEAN) {
            distCell.setCellValue(srcCell.getBooleanCellValue());
        } else if (srcCellType == CellType.ERROR) {
            distCell.setCellErrorValue(srcCell.getErrorCellValue());
        } else if (srcCellType == CellType.FORMULA) {
            distCell.setCellFormula(srcCell.getCellFormula());
        } else { // nothing29

        }
    }
}


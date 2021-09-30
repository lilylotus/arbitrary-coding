package cn.nihility.util;

import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;

public class ExcelXlsxUtil {
    /**
     * 复制行
     *
     * @param startCol  起始列
     * @param endCol    结束列
     * @param pPosition 目标起始行位置
     */
    public static XSSFSheet copycols(XSSFSheet currentSheet, int startCol, int endCol, int pPosition, boolean isdyn) {
        if (isdyn) {
            int coloumNum = currentSheet.getRow(0).getPhysicalNumberOfCells();
            copycols(currentSheet, pPosition, coloumNum - 1, pPosition + endCol - startCol + 1, false);
        }
        int pStartCol = startCol;
        int pEndCol = endCol;
        int targetColFrom;
        int targetColTo;
        CellRangeAddress region;
        int i;
        int j;
        if (pStartCol == -1 || pEndCol == -1) {
            return null;
        }
        int numMergedRegions = currentSheet.getNumMergedRegions();
        for (i = 0; i < numMergedRegions; i++) {
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
        int rowNum = currentSheet.getLastRowNum();
        for (i = 0; i <= rowNum; i++) {
            XSSFRow sourceRow = currentSheet.getRow(i);
            if (sourceRow != null) {
                XSSFRow newRow = currentSheet.getRow(i);
                for (j = pEndCol; j >= pStartCol; j--) {
                    XSSFCell templateCell = sourceRow.getCell(j);
                    if (i == 0) {
                        currentSheet.setColumnWidth(pPosition + j - pStartCol, currentSheet.getColumnWidth(j));
                    }
                    if (templateCell != null) {
                        XSSFCell newCell = newRow.createCell(pPosition + j - pStartCol);
                        copyCell(templateCell, newCell);
                    }
                }
            }
        }
        return currentSheet;
    }

    public static void copyRows(XSSFSheet currentSheet, int startRow, int endRow, int pPosition) {
        int pStartRow = startRow - 1;
        int pEndRow = endRow - 1;
        int targetRowFrom;
        int targetRowTo;
        int columnCount;
        CellRangeAddress region;
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
            XSSFRow sourceRow = currentSheet.getRow(i);
            columnCount = sourceRow.getLastCellNum();
            if (sourceRow != null) {
                XSSFRow newRow = currentSheet.createRow(pPosition - pStartRow
                    + i);
                newRow.setHeight(sourceRow.getHeight());
                for (j = 0; j < columnCount; j++) {
                    XSSFCell templateCell = sourceRow.getCell(j);
                    if (templateCell != null) {
                        XSSFCell newCell = newRow.createCell(j);
                        copyCell(templateCell, newCell);
                    }
                }
            }
        }
    }

    private static void copyCell(XSSFCell srcCell, XSSFCell distCell) {
        distCell.setCellStyle(srcCell.getCellStyle());
        if (srcCell.getCellComment() != null) {
            distCell.setCellComment(srcCell.getCellComment());
        }
        CellType srcCellType = srcCell.getCellType();
        try {
            distCell.setCellType(srcCellType);
        } catch (Exception e) {
            e.printStackTrace();
        }
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


package cn.nihility.util;


import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;

public class POIUtil {
    /**
     * 复制行
     *
     * @param startRow  开始行
     * @param endRow    结束行
     * @param pPosition 目标行
     * @param sheet     工作表对象
     */
    public static void copyRows(int startRow, int endRow, int pPosition, XSSFSheet sheet) {
        int pStartRow = startRow;
        int pEndRow = endRow;
        int targetRowFrom;
        int targetRowTo;
        int columnCount;
        CellRangeAddress region = null;
        int i;
        int j;
        if (pStartRow == -1 || pEndRow == -1) {
            return;
        }
        // 拷贝合并的单元格
        for (i = 0; i < sheet.getNumMergedRegions(); i++) {
            region = sheet.getMergedRegion(i);
            if ((region.getFirstRow() >= pStartRow) && (region.getLastRow() <= pEndRow)) {
                targetRowFrom = region.getFirstRow() - pStartRow + pPosition;
                targetRowTo = region.getLastRow() - pStartRow + pPosition;
                CellRangeAddress newRegion = region.copy();
                newRegion.setFirstRow(targetRowFrom);
                newRegion.setFirstColumn(region.getFirstColumn());
                newRegion.setLastRow(targetRowTo);
                newRegion.setLastColumn(region.getLastColumn());
                sheet.addMergedRegion(newRegion);
            }
        }
        // 设置列宽
        for (i = pStartRow; i <= pEndRow; i++) {
            XSSFRow sourceRow = sheet.getRow(i);
            columnCount = sourceRow.getLastCellNum();
            if (sourceRow != null) {
                XSSFRow newRow = sheet.createRow(pPosition - pStartRow + i);
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

    /**
     * 复制单元格
     *
     * @param srcCell  原始单元格
     * @param distCell 目标单元格
     */
    public static void copyCell(XSSFCell srcCell, XSSFCell distCell) {
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

    /**
     * 表格中指定位置插入行
     *
     * @param sheet    工作表对象
     * @param rowIndex 指定的行数
     * @return 当前行对象
     */
    public static XSSFRow insertRow(XSSFSheet sheet, int rowIndex) {
        XSSFRow row = null;
        if (sheet.getRow(rowIndex) != null) {
            int lastRowNo = sheet.getLastRowNum();
            sheet.shiftRows(rowIndex, lastRowNo, 1);
        }
        row = sheet.createRow(rowIndex);
        return row;
    }
}


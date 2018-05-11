package com.accenture.goep.ValidatePDP;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.text.ParseException;
import javax.imageio.ImageIO;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.openqa.selenium.Cookie;

import ru.yandex.qatools.allure.annotations.Step;
import ru.yandex.qatools.ashot.comparison.ImageDiff;
import ru.yandex.qatools.ashot.comparison.ImageDiffer;

public class ProductValidationWithMethods {
	static WebDriver driver = null;
	private static Map<String, String> resultsMap = new HashMap<String, String>();
	private static HashSet<String> keyset = new HashSet<String>();
//	private static Map<int, String[]> resultsMap = new HashMap<int, String[]>();
    private static String variant = null;
	

	@Test
	private static void start() throws Exception {

		System.setProperty("webdriver.chrome.driver",System.getProperty("user.dir") + "\\utils\\chromedriver.exe");
		driver = new ChromeDriver();
		driver.manage().window().maximize();
		openUrl();

	}
     @Step("Open Url and handle Notifications")
	private static void openUrl() throws InterruptedException, ParseException {

    	 //Handle Notifications at different environments( int and tst)
          driver.get("http://int-www2.hm.com/en_gb/index.html");
          Thread.sleep(1000);
          if(isElementPresent())
          driver.findElement(By.xpath("//button[@class='close icon-close-white js-close']")).click();
          if(isElementPresentNotification())
          driver.findElement(By.xpath("//button[@class='button modalconfirm modalclose js-read-gdpr']")).click();
          Thread.sleep(1000);  
          
          driver.get("http://tst-www2.hm.com/en_gb/index.html");
          Thread.sleep(1000);
          if(isElementPresent())
              driver.findElement(By.xpath("//button[@class='close icon-close-white js-close']")).click();
		
          //Getting the url from set and checking whether the item is present or not 
		try {
			for (String url : keyset) {

				driver.get(url);
                System.out.println("Opening the url: "+url);
				//Handling URL for MultiCountries
				if (url.contains("en_eur")) {

				
						addMultiCountryCookieOnEntrancePage("el_GR"); // Greece
						driver.get(url);
					
				} else if (url.contains("en_asia1")) {
					
						addMultiCountryCookieOnEntrancePage("en_HK"); // Hongkong
						driver.get(url);
					
				}

				else if (url.contains("en_asia2")) {
				
						addMultiCountryCookieOnEntrancePage("en_SG"); // Singapore
						driver.get(url);
					
				} else if (url.contains("en_asia3")) {
					
						addMultiCountryCookieOnEntrancePage("en_TW"); // Taiwan
						driver.get(url);
					
				} else if (url.contains("en_asia4")) {
					
						addMultiCountryCookieOnEntrancePage("ms_MY"); // Malaysia
						driver.get(url);
					
				} else if (url.contains("en_asia5")) {
					
						addMultiCountryCookieOnEntrancePage("en_PH"); // philipines
						driver.get(url);
				}

				Thread.sleep(2000);
				if ((driver.findElements(By.xpath("//div[@class='module product-description sticky-wrapper']"))
						.size() != 0)) {
                // Verify whether the product image is displayed or not 
					verifyProductImage(driver);
				} else {
					resultsMap.put(driver.getCurrentUrl(), "No Item Found error");
				}

			}
		} catch (InterruptedException e) {
			e.printStackTrace();

		}
	}
    @Step("Comparing Every Product Image with the H&M Reference Image") 
	private static void verifyProductImage(WebDriver driver) throws InterruptedException {
		String imageUrl = driver.findElement(By.xpath("//*[@id=\"main-content\"]/div[1]/div[2]/div[1]/figure/div/img"))
				.getAttribute("src");
		BufferedImage expectedImage;
		try {

			Thread.sleep(3000);
			URL url = new URL(imageUrl);
			InputStream is = url.openStream();
			Image image = ImageIO.read(is);
			String prod_id = driver.getCurrentUrl().substring(42, 52);
			SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH-mm-ss");
			Date date = new Date();
			File destinationFile = new File(System.getProperty("user.dir") + "\\screenshots\\" + dateFormat.format(date)
					+ "_" + prod_id + ".jpg");
			ImageIO.write((RenderedImage) image, "jpg", destinationFile);
			expectedImage = ImageIO.read(new File(System.getProperty("user.dir") + "\\utils\\image.jpg"));
			ImageDiffer imgDiff = new ImageDiffer();
			//Ashot method to compare the images
			ImageDiff diff = imgDiff.makeDiff((BufferedImage) image, expectedImage);
			if (diff.hasDiff()) 
				//getting the size of each variant of the products
				verifySizeIsEnabled(driver, "Product Image is displayed");

		    else 
				verifySizeIsEnabled(driver, "No Image is displayed");

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Step("Getting the size of each variant and checking for the stock(Few pieces left/Out of Stock)")  
	private static void verifySizeIsEnabled(WebDriver driver, String image) {
		String sizeResult = null;

		try {
			Thread.sleep(2000);
			if (driver.findElements(By.xpath("//button[@class='close icon-close-white js-close']")).size() != 0) {
				driver.findElement(By.xpath("//button[@class='close icon-close-white js-close']")).click();
			}
			Thread.sleep(1000);
			if (driver
					.findElement(
							By.xpath("//div[@class='product-item-buttons']//div[contains(@class,'picker')]/button"))
					.isEnabled()) {

				driver.findElement(
						By.xpath("(//div[@class='product-item-buttons']//div[contains(@class,'picker')]/button)[1]"))
						.click();

				// Getting all the sizes in a list
				List<WebElement> listSizes = driver.findElements(
						By.xpath("//div[@class='product-item-buttons']//div[contains(@class,'picker')]/ul/li"));
				for (int i = 1; i < listSizes.size(); i++) {
					Boolean stockLeft = elementExists(
							By.xpath("//div[@class='product-item-buttons']//div[contains(@class,'picker')]/ul/li["
									+ (i + 1) + "]//span[2]"));
					if (stockLeft == true) {
						//Getting whether the item is out of stock or few pieces left
						sizeResult = checkStock(i);

					} else {

						sizeResult = "Size Available";
					}
					variant = driver
							.findElement(By.xpath("//ul[@class='picker-list is-inline']/li" + "[" + (i + 1) + "]"))
							.getAttribute("data-code");
					resultsMap.put(driver.getCurrentUrl() + "," + variant, image + "/" + sizeResult);

				}
			} else {
				if (driver.findElement(By.xpath("//button[contains(@class,'button-big button-buy')]")).isEnabled()) {
					variant = "No Size";
					sizeResult = "Add to cart button is enabled";
					resultsMap.put(driver.getCurrentUrl() + "," + variant, image + "/" + sizeResult);
				} else {
					variant = "No Size";
					sizeResult = "Out of Stock";
					resultsMap.put(driver.getCurrentUrl() + "," + variant, image + "/" + sizeResult);

				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static boolean elementExists(By locator) {
		return !driver.findElements(locator).isEmpty();
	}

	public static String getText(WebDriver driver, WebElement element) {
		return (String) ((JavascriptExecutor) driver).executeScript("return jQuery(arguments[0]).text();", element);
	}

	@BeforeTest
	@Step(" Reading the data from excel and store the data into the hash set")
	public void readExcel() {
		try {
			File folder = new File(System.getProperty("user.dir") + "\\AutomationOutput");
			if (!folder.exists()) {
				folder.mkdirs();
				System.out.println("Created the output folder");
			} else {
				System.out.println("Folder already exists");
			}

			File f = new File(System.getProperty("user.dir") +"\\utils\\Test_Data.xlsx");
			FileInputStream fileIn = new FileInputStream(f);

			XSSFWorkbook workbook = new XSSFWorkbook(fileIn);
			// open sheet 0 which is first sheet of your worksheet
			XSSFSheet sheet = workbook.getSheetAt(0);

			for (Row row : sheet) {
				if (row.getRowNum() == 0) {
					continue;
				} else {
					Cell c = row.getCell(0);
					
					if (c == null) {
						// Nothing in the cell in this row, skip it
				
					} else {

						keyset.add(c.getStringCellValue());

					}
				}

			}
			
			System.out.println("/****The size of Key set is:" + keyset.size() + "************/");
			//workbook.close;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@AfterTest
	@Step("Generating the Output Sheet in the Automation Output Folder")
	private static void printResults() {
		driver.quit();
		

		try {
			SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH-mm-ss");
			Date date = new Date();
			FileOutputStream out = new FileOutputStream(
					new File(System.getProperty("user.dir") + "\\AutomationOutput\\"
							+ dateFormat.format(date) + "_Automation Report.xlsx"));

			XSSFWorkbook workbook2 = new XSSFWorkbook();
			XSSFSheet sheet2 = workbook2.createSheet("Test_Report");

			XSSFCellStyle style = (XSSFCellStyle) workbook2.createCellStyle();
			style.setFillForegroundColor(new XSSFColor(new java.awt.Color(255, 0, 0)));
			style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

			XSSFCellStyle stylesuccess = (XSSFCellStyle) workbook2.createCellStyle();
			stylesuccess.setFillForegroundColor(new XSSFColor(new java.awt.Color(0, 255, 0)));
			stylesuccess.setFillPattern(FillPatternType.SOLID_FOREGROUND);

			Row row = sheet2.createRow(0);
			int rownum = 1;
			Cell cell1 = row.createCell(0);

			cell1.setCellValue("URL");

			Cell cell2 = row.createCell(1);
			cell2.setCellValue("Message");
			/*
			 * sheet2.setColumnWidth(0, 12000); sheet2.setColumnWidth(1, 12000);
			 */

			for (String key : resultsMap.keySet()) {

				row = sheet2.createRow(rownum++);
				Cell cellfirst = row.createCell(0);
				cellfirst.setCellValue((String) key);
				Cell cellsecond = row.createCell(1);
				cellsecond.setCellValue(resultsMap.get(key));
				if (resultsMap.get(key).contains("Product Image is displayed")) {
			
					cellsecond.setCellStyle(stylesuccess);
				}
				if (resultsMap.get(key).contains("error") || resultsMap.get(key).contains("No Image")
						|| resultsMap.get(key).contains("Stock")) {
					
					cellsecond.setCellStyle(style);
				}
			}

			autoSizeColumns(workbook2);
			workbook2.write(out);
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
   @Step("Formatting the Output Sheet")  
	public static void autoSizeColumns(Workbook workbook) {
		int numberOfSheets = workbook.getNumberOfSheets();
		for (int i = 0; i < numberOfSheets; i++) {
			org.apache.poi.ss.usermodel.Sheet sheet = workbook.getSheetAt(i);
			if (sheet.getPhysicalNumberOfRows() > 0) {
				Row row = sheet.getRow(0);
				Iterator<Cell> cellIterator = row.cellIterator();
				while (cellIterator.hasNext()) {
					Cell cell = cellIterator.next();
					int columnIndex = cell.getColumnIndex();
					sheet.autoSizeColumn(columnIndex);
				}
			}
		}
	}

	@Step("Add market specific cookie to the browser")
	public static void addMultiCountryCookieOnEntrancePage(String Market_Category) throws ParseException {

		Date date = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy").parse("Sat Nov 26 15:14:00 IST 2018");
		driver.manage().addCookie(new Cookie("HMCORP_locale", Market_Category, ".hm.com", "/", date));

	}
    @Step("Check product's stock")
	public static String checkStock(int i) {
		String res;
		String classname = driver.findElement(By.xpath(
				"//div[@class='product-item-buttons']//div[contains(@class,'picker')]/ul/li[" + (i + 1) + "]//span[2]"))
				.getAttribute("class");
		
		if (classname.equalsIgnoreCase("info warning")) 
			res = "Few Pieces Left";
		
		else 
			res = "Out Of Stock";
			
return res;

	}
	public static boolean isElementPresent() {
        
        try {
            driver.findElement(By.xpath("//button[@class='close icon-close-white js-close']")).isDisplayed();
            return true;
        } catch (Exception e) {
            return false;
        }
	}
public static boolean isElementPresentNotification() {
        
        try {
        	driver.findElement(By.xpath("//button[@class='button modalconfirm modalclose js-read-gdpr']")).isDisplayed();
            return true;
        } catch (Exception e) {
            return false;
        }
	}


}

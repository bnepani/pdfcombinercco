package com.appirio;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.apache.poi.hslf.model.Slide;
import org.apache.poi.hslf.usermodel.SlideShow;
import org.apache.poi.poifs.filesystem.DirectoryEntry;
import org.apache.poi.poifs.filesystem.DocumentEntry;
import org.apache.poi.poifs.filesystem.DocumentInputStream;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.detect.DefaultDetector;
import org.apache.tika.detect.Detector;
import org.apache.tika.extractor.ContainerExtractor;
import org.apache.tika.extractor.EmbeddedDocumentExtractor;
import org.apache.tika.extractor.EmbeddedResourceHandler;
import org.apache.tika.extractor.ParserContainerExtractor;
import org.apache.tika.io.IOUtils;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Font.FontFamily;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.html.simpleparser.HTMLWorker;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfWriter;
/**
 * @author jesus
 *
 */
public class PDFCombiner {

	// unique identifier for generated files
	private String uniqueId = null;

	// generated pdf directory (generated at runtime)
	private String generatedPDFDir = null;

	// list of temporary files that will be deleted
	private List<String> temporaryFilesToBeDeleted = null;

	private static final String PDFS_TEMPLATES_DIR = System.getenv("PDFS_TEMPLATES_DIR");

	/**
	 * Creates a directory to store generated pdf and temporary files (as images, etc.)
	 * @throws Exception
	 */
	public String getGeneratedPDFDir() throws Exception {
		if(generatedPDFDir == null) {
			generatedPDFDir = System.getenv("GENERATED_PDFS_DIR") + File.separator + this.getUniqueId();

			if(!new File(generatedPDFDir).mkdirs())
			{
				throw new Exception("Could not create working directory " + generatedPDFDir);
			}
		}

		return generatedPDFDir;
	}

	/**
	 * Returns a unique id for this job
	 * @return
	 */
	public String getUniqueId() {
		if(uniqueId == null) {
			uniqueId = String.valueOf(Calendar.getInstance().getTimeInMillis());
		}

		return uniqueId;
	}

	/**
	 * Return combined PDF file name
	 * @throws Exception
	 */
	public String getCombinedFileName() throws Exception {
		return this.getGeneratedPDFDir() + File.separator + this.getUniqueId() + ".pdf";
	}

	/**
	 * Remove generated files
	 * @throws Exception
	 */
	public void deleteGeneratedFiles() throws Exception {
		// delete files
		for(File file : new File(this.getGeneratedPDFDir()).listFiles()) {
			if(!file.delete()) {
				throw new Exception("Could not delete file " + file.getAbsolutePath());
			}
		}

		// delete parent directory
		if(!new File(this.getGeneratedPDFDir()).delete()) {
			throw new Exception("Could not delete directory " + this.getGeneratedPDFDir());
		}
	}

	/**
	 * Combine multiple files into a single PDF. Accepts office formats: doc, docx, xls, xlsx, ppt. PDF file format. Image formats accepted by itext.
	 */
	public String combine(PDFCombinerArguments pdfCombinerArguments) throws Exception {

		String combinedFileName = null;

        // merge pdf
		LinkedHashMap<String, PDFCombinerFile> pdfFileList = new LinkedHashMap<String, PDFCombinerFile>();
        temporaryFilesToBeDeleted = new ArrayList<String>();

        // create pdf for contents
    	if(pdfCombinerArguments.getContents() != null && !pdfCombinerArguments.isExcludeFlightLines()) {
	        LinkedHashMap<String, PDFCombinerFile> contentsPdfsList = createPdfsForCollection(pdfCombinerArguments.getContents());
	        pdfFileList.putAll(contentsPdfsList);

	        // every item in content is a proposal report. set IsProposalReport to true.
	        for(PDFCombinerFile pdfCombinerFile : pdfCombinerArguments.getContents()) {
	        	pdfCombinerFile.setIsProposalReport(true);
	        }
    	}

        // create pdf for appendixes
    	if(pdfCombinerArguments.getAppendixes() != null) {
	        LinkedHashMap<String, PDFCombinerFile> appendixesPdfsList = createPdfsForCollection(pdfCombinerArguments.getAppendixes());
	        pdfFileList.putAll(appendixesPdfsList);
    	}

        // combine temporary pdfs into a single pdf
        if(pdfFileList.size() > 0 || pdfCombinerArguments.isExcludeFlightLines()) {

    		// create a top part list for storing references to coverpage and toc
    		LinkedHashMap<String, PDFCombinerFile> pdfCompleteFileList = new LinkedHashMap<String, PDFCombinerFile>();

            // Resulting pdf
            OutputStream out = new FileOutputStream(new File(this.getCombinedFileName()));

            // coverage page
            if(pdfCombinerArguments.isShowCoverPage()) {
	            String coverpageFileName = getGeneratedCoverpagePdfPath(pdfCombinerArguments);
	        	temporaryFilesToBeDeleted.add(coverpageFileName);
	        	pdfCompleteFileList.put(coverpageFileName, null);
            }	
            // table of contents
        	if(pdfCombinerArguments.isShowTableOfContents() && pdfFileList.size() > 0 ){//!pdfCombinerArguments.isExcludeFlightLines()) {
	        	String tocFileName = getGeneratedTOCPdfPath(pdfCombinerArguments);
	        	temporaryFilesToBeDeleted.add(tocFileName);
	        	pdfCompleteFileList.put(tocFileName, null);
        	}

        	// add to final list
        	if(pdfFileList.size() > 0)
        	pdfCompleteFileList.putAll(pdfFileList);

        	// merge pdfs
            doMerge(pdfCombinerArguments, pdfCompleteFileList, out);

            deleteTempFiles(temporaryFilesToBeDeleted);

            combinedFileName = this.getCombinedFileName();
            System.out.println("   combinedFileName: " + combinedFileName);
        }

        // return combined pdf filename
        return combinedFileName;
	}

	private LinkedHashMap<String, PDFCombinerFile> createPdfsForCollection(List<PDFCombinerFile> files) throws Exception {

		LinkedHashMap<String, PDFCombinerFile> pdfFileList = new LinkedHashMap<String, PDFCombinerFile>();
		
        // iterate passed files parameter. use filename extension to determine conversion method.
        for(PDFCombinerFile file : files) {
            if(file.getFileName().toLowerCase().endsWith("pdf")) {
                pdfFileList.put(file.getFileName(), file);
                file.setNumberOfPages(getNumberOfPagesInPdf(file.getFileName()));
            } else if(file.getFileName().toLowerCase().endsWith("doc") || file.getFileName().toLowerCase().endsWith("docx") || file.getFileName().toLowerCase().endsWith("xls") || file.getFileName().toLowerCase().endsWith("xlsx")) {
            	String fileName = convertOfficeFileToPDF(file.getFileName());
            	temporaryFilesToBeDeleted.add(fileName);
            	pdfFileList.put(fileName, file);
            	file.setNumberOfPages(getNumberOfPagesInPdf(fileName));
            } else if(file.getFileName().toLowerCase().endsWith("ppt")) {
            	LinkedHashMap<String, PDFCombinerFile> pptPdfs = convertPPT(file.getFileName(), file);
            	pdfFileList.putAll(pptPdfs);
            	file.setNumberOfPages(pptPdfs.size());
            } else {
            	String fileName = convertImageToPDF(file.getFileName());
            	temporaryFilesToBeDeleted.add(fileName);
            	pdfFileList.put(fileName, file);
            	file.setNumberOfPages(getNumberOfPagesInPdf(fileName));
            }
        }

        return pdfFileList;

	}

	/**
	 * Returns number of pages in specified PDF file
	 */
	private Integer getNumberOfPagesInPdf(String pdfFileName) throws IOException {

    	InputStream in = new FileInputStream(pdfFileName);
        PdfReader reader = new PdfReader(in);
        return reader.getNumberOfPages();
	}

	/**
	 * Convert PPT to pdf
	 * @throws Exception
	 */
	private LinkedHashMap<String, PDFCombinerFile> convertPPT(String file, PDFCombinerFile pdfCombinerFile) throws Exception {
	    FileInputStream is = new FileInputStream(file);
	    SlideShow ppt = new SlideShow(is);
	    is.close();

	    LinkedHashMap<String, PDFCombinerFile> generatedImageFiles = new LinkedHashMap<String, PDFCombinerFile>();

	    Dimension pgsize = ppt.getPageSize();
	
	    Slide[] slide = ppt.getSlides();
	    for (int i = 0; i < slide.length; i++) {
	
	        BufferedImage img = new BufferedImage(pgsize.width, pgsize.height, BufferedImage.TYPE_INT_RGB);
	        Graphics2D graphics = img.createGraphics();
	        //clear the drawing area
	        graphics.setPaint(Color.white);
	        graphics.fill(new Rectangle2D.Float(0, 0, pgsize.width, pgsize.height));
	
	        //render
	        slide[i].draw(graphics);
	
	        //save the output
	        File tempFile = File.createTempFile("tmp-slide-" + (i+1), ".png", new File(this.getGeneratedPDFDir()));
	        FileOutputStream out = new FileOutputStream(tempFile);
	        javax.imageio.ImageIO.write(img, "png", out);
	        out.close();

	        // add file to list
	        String pdfFile = convertImageToPDF(tempFile.getAbsolutePath());

	        generatedImageFiles.put(pdfFile, pdfCombinerFile);
	    }

        return generatedImageFiles;
	}

	/**
	 * Convert Office file format to pdf
	 * @param file
	 * @throws Exception
	 */
	private String convertOfficeFileToPDF(String file) throws Exception {
		String result = getXML(file).xml;

       ContainerExtractor extractor = new ParserContainerExtractor();
       TrackingHandler handler;

       handler = process(file, extractor, false);

       System.out.println("   result... ");
       System.out.println(result);

	   String directory = new File(this.getGeneratedPDFDir()).getAbsolutePath() + File.separator;
	   result = result.replace("embedded:", directory);

	   if(handler.filenames.size() > 0) {
		   System.out.println("   result (after): " + result);
	   }


       Document document = new Document();

       File tempFile = File.createTempFile("temp_", ".pdf", new File(this.getGeneratedPDFDir()));

       String generatedPDF = tempFile.getAbsolutePath();// getGeneratedPDFDir() + "\\NonPDFContent.pdf";

       PdfWriter.getInstance(document, new FileOutputStream(generatedPDF));

       document.open();

       // add result elements to pdf 
	    List<Element> objects
	        = HTMLWorker.parseToList(
	            new StringReader(result), null, null);
	    for (Element element : objects) {
	        document.add(element);
	    }

        // closes document
        document.close();

        return generatedPDF;
	}

	/**
	 * Extaract embedded files from document
	 */
    private class FileEmbeddedDocumentExtractor implements EmbeddedDocumentExtractor {
		private int count = 0;
        private final TikaConfig config = TikaConfig.getDefaultConfig();
        private String generatedPDFDir;

		public FileEmbeddedDocumentExtractor(String generatedPDFDir) {
			this.generatedPDFDir = generatedPDFDir;
		}

		@Override
		public void parseEmbedded(InputStream inputStream, ContentHandler contentHandler, Metadata metadata, boolean outputHtml)
				throws SAXException, IOException {

			// base name
            String name = metadata.get(Metadata.RESOURCE_NAME_KEY);

            // create name if name is not available
            if (name == null) {
                name = "file" + count++;
            }

            // determine content type
            MediaType contentType = detector.detect(inputStream, metadata);

            if (name.indexOf('.')==-1 && contentType!=null) {
                try {
                    name += config.getMimeRepository().forName(
                            contentType.toString()).getExtension();
                } catch (MimeTypeException e) {
                    e.printStackTrace();
                }
            }

            // to global generated pdf dir
            File outputFile = new File(this.getGeneratedPDFDir(), name);
            File parent = outputFile.getParentFile();
            if (!parent.exists()) {
                if (!parent.mkdirs()) {
                    throw new IOException("unable to create directory \"" + parent + "\"");
                }
            }
            System.out.println("Extracting '"+name+"' ("+contentType+") to " + outputFile);

            // copy tika files to outputFile location
            FileOutputStream os = new FileOutputStream(outputFile);

            if (inputStream instanceof TikaInputStream) {
                TikaInputStream tin = (TikaInputStream) inputStream;

                if (tin.getOpenContainer() != null && tin.getOpenContainer() instanceof DirectoryEntry) {
                    POIFSFileSystem fs = new POIFSFileSystem();
                    copy((DirectoryEntry) tin.getOpenContainer(), fs.getRoot());
                    fs.writeFilesystem(os);
                } else {
                    IOUtils.copy(inputStream, os);
                }
            } else {
                IOUtils.copy(inputStream, os);
            }

            os.close();

		}

		@Override
		public boolean shouldParseEmbedded(Metadata arg0) {

			return true;
		}
		
        protected void copy(DirectoryEntry sourceDir, DirectoryEntry destDir)
                throws IOException {
            for (org.apache.poi.poifs.filesystem.Entry entry : sourceDir) {
                if (entry instanceof DirectoryEntry) {
                    // Need to recurse
                    DirectoryEntry newDir = destDir.createDirectory(entry.getName());
                    copy((DirectoryEntry) entry, newDir);
                } else {
                    // Copy entry
                    InputStream contents = new DocumentInputStream((DocumentEntry) entry);
                    try {
                        destDir.createDocument(entry.getName(), contents);
                    } finally {
                        contents.close();
                    }
                }
            }
        }

        // get generated pdf dir
		private String getGeneratedPDFDir() {
			return this.generatedPDFDir;
		}
    }

    /** Inner class to add a header and a footer. */
    static class HeaderFooter extends PdfPageEventHelper {

    	// start page count at this page
    	int startPageNumber = 0;

    	public HeaderFooter(int startPageNumber) {
    		this.startPageNumber = startPageNumber;
    	}

        public void onEndPage (PdfWriter writer, Document document) {
            Rectangle rect = writer.getPageSize();
            
            int contentPage = writer.getPageNumber() - this.startPageNumber;
            if(contentPage > 0) {
	            ColumnText.showTextAligned(writer.getDirectContent(),
	                    Element.ALIGN_LEFT, 
	                    new Phrase(0.0F, String.format("Page %d", contentPage), new Font(FontFamily.HELVETICA, 7)),
	                    rect.getLeft() + 10, rect.getBottom() + 18, 0);
            }
        }
    }
    static class AEFooter extends PdfPageEventHelper {
    	// start page count at this page
    	int startPageNumber = 0;
    	String aeName = "";
    	public AEFooter(int startPageNumber, String aeName) {
    		this.startPageNumber = startPageNumber;    		
    		this.aeName = aeName.substring(0, aeName.indexOf("\n"));
    	}

        public void onEndPage (PdfWriter writer, Document document) {
            Rectangle rect = writer.getPageSize();

            int contentPage = writer.getPageNumber() - this.startPageNumber;
            System.out.println("AE Name->"+ aeName);
            if(contentPage > 0) {
	            ColumnText.showTextAligned(writer.getDirectContent(),
	                    Element.ALIGN_CENTER, 
	                    new Phrase(0.0F, String.format("Prepared by: %s", aeName), new Font(FontFamily.HELVETICA, 7)),
	                    (rect.getLeft() + rect.getRight()) / 2, rect.getBottom() + 18, 0);
            }
        }
    }


    static class DateTimeFooter extends PdfPageEventHelper {

    	// start page count at this page
    	int startPageNumber = 0;

    	private String dateTimeStamp;

    	public DateTimeFooter(String dateTimeStamp, int startPageNumber) {
    		this.startPageNumber = startPageNumber;
    		setDateTimeStamp(dateTimeStamp);
    	}

        public void onEndPage (PdfWriter writer, Document document) {
            Rectangle rect = writer.getPageSize();

            int contentPage = writer.getPageNumber() - this.startPageNumber;
            if(contentPage > 0) {
            	System.out.println("   getDateTimeStamp(): " + getDateTimeStamp() + " contentPage: " + contentPage);
	            ColumnText.showTextAligned(writer.getDirectContent(),
	                    Element.ALIGN_RIGHT, new Phrase(0.0F, String.format("%s", getDateTimeStamp()), new Font(FontFamily.HELVETICA, 7)),
	                    rect.getRight() - 20, rect.getBottom() + 18, 0);
            }
        }

		public String getDateTimeStamp() {
			return dateTimeStamp;
		}

		public void setDateTimeStamp(String dateTimeStamp) {
			this.dateTimeStamp = dateTimeStamp;
		}
    }

    // set xml contents
    private static class XMLResult {
        public final String xml;
        public XMLResult(String xml, Metadata metadata) {
            this.xml = xml;
        }
    }

    Detector detector;
    
    // get conents as xml
    private XMLResult getXML(String filePath) throws Exception {
        InputStream input = null;
        Metadata metadata = new Metadata();
        detector = new DefaultDetector();
        Parser parser = new AutoDetectParser(detector);

        // get xml contents from metadata
        StringWriter sw = new StringWriter();
        SAXTransformerFactory factory = (SAXTransformerFactory)
                 SAXTransformerFactory.newInstance();
        TransformerHandler handler = factory.newTransformerHandler();
        handler.getTransformer().setOutputProperty(OutputKeys.METHOD, "xml");
        handler.getTransformer().setOutputProperty(OutputKeys.INDENT, "no");
        handler.setResult(new StreamResult(sw));

        ParseContext context = new ParseContext();
        context.set(Parser.class, parser);
        context.set(EmbeddedDocumentExtractor.class, new FileEmbeddedDocumentExtractor(this.getGeneratedPDFDir()));

        input =new FileInputStream(new File(filePath));
        try {
            parser.parse(input, handler, metadata, context);
            return new XMLResult(sw.toString(), metadata);
        } finally {
            input.close();
        }
    }

	/**
	 * Convert an image to PDF
	 * @throws DocumentException
	 * @throws MalformedURLException
	 * @throws IOException
	 * @throws Exception
	 */
	private String convertImageToPDF(String file) throws DocumentException, MalformedURLException, IOException, Exception {
        Image image1 = Image.getInstance(file);

		Document document = new Document();

		//if you would have a chapter indentation
		int indentation = 0;
		//whatever

		// scale to fit page size
		float scaler = ((document.getPageSize().getWidth() - document.leftMargin()
		               - document.rightMargin() - indentation) / image1.getWidth()) * 100;

		image1.scalePercent(scaler);

		// give it a name
        File tempFile = File.createTempFile("temp_", ".pdf", new File(this.getGeneratedPDFDir()));

        // get absolute path
        String generatedPDF = tempFile.getAbsolutePath();

        // add content to pdf
        PdfWriter.getInstance(document, new FileOutputStream(generatedPDF));

        document.open();

        document.add(image1);

        // closes document
        document.close(); // no need to close PDFwriter?

        return generatedPDF;
	}

	// embedded content detection handler
	private static class TrackingHandler implements EmbeddedResourceHandler {
        public List<String> filenames = new ArrayList<String>();
        public List<MediaType> mediaTypes = new ArrayList<MediaType>();
        
        public void handle(String filename, MediaType mediaType,
             InputStream stream) {
           filenames.add(filename);
           mediaTypes.add(mediaType);
       }
     }

	private TikaInputStream getTestFile(String filename) throws Exception {
        InputStream input = new FileInputStream(new File(filename));

        return TikaInputStream.get(input);
    }

	// return a tracking handler for a filename. (embedded content detection)
	private TrackingHandler process(String filename, ContainerExtractor extractor, boolean recurse) throws Exception {
        TikaInputStream stream = getTestFile(filename);
        try {

            // Process it
            TrackingHandler handler = new TrackingHandler();
            if(recurse) {
                extractor.extract(stream, extractor, handler);
            } else {
                extractor.extract(stream, null, handler);
            }

            // So they can check what happened
            return handler;
        } finally {
            stream.close();
        }
    }
    

	private void deleteTempFiles(List<String> tempFiles) {
		for(String tempFile : tempFiles) {
			new File(tempFile).delete();
		}
	}

	// return page numbers for pdfFiles
	private static List<Integer> getPageNumbers(List<String> pdfFileNamesList, int startPage) throws IOException {

		List<Integer> pageNumbers = new ArrayList<Integer>();

		Integer pageNumber = startPage;

        for (String pdfFileName : pdfFileNamesList) {
        	InputStream in = new FileInputStream(pdfFileName);
            PdfReader reader = new PdfReader(in);
            pageNumber = pageNumber + reader.getNumberOfPages();
            pageNumbers.add(pageNumber);
        }

		return pageNumbers;
	}

	private static int getStartPageNumber(PDFCombinerArguments pdfCombinerArguments) {
		int start = 0;
		if(pdfCombinerArguments.isShowCoverPage()) start++;
		if(pdfCombinerArguments.isShowTableOfContents()) start++;
		return start;
	}

	
	/**
     * Merge multiple pdf into one pdf
     *
     * @param list of pdf file names to be merged
     * @param outputStream output file output stream
     * @throws DocumentException
     * @throws IOException
     */
	private static void doMerge(PDFCombinerArguments pdfCombinerArguments, LinkedHashMap<String, PDFCombinerFile> pdfFileList, OutputStream outputStream)
            throws DocumentException, IOException {
        Document document = new Document();
        document.setPageSize(PageSize.A4);
        PdfWriter writer = PdfWriter.getInstance(document, outputStream);
        writer.setBoxSize("art", PageSize.A4);

        int startPageNo = getStartPageNumber(pdfCombinerArguments);
		if(pdfCombinerArguments.isShowPageNumbering()) {
        	HeaderFooter event = new HeaderFooter(startPageNo);
        	writer.setPageEvent(event);
        }
		
		AEFooter aeEvent;
		if(pdfCombinerArguments.getMarketContactInformation() != null)
			aeEvent = new AEFooter(startPageNo, pdfCombinerArguments.getMarketContactInformation());
		else
			aeEvent = new AEFooter(startPageNo, pdfCombinerArguments.getClientContactInformation());
		
		writer.setPageEvent(aeEvent);

        if(pdfCombinerArguments.isShowTimeAndDateStamp()) {
        	DateTimeFooter dateTimeFooterEvent = new DateTimeFooter(pdfCombinerArguments.getDateTimeStamp(), startPageNo);
        	writer.setPageEvent(dateTimeFooterEvent);
        }

        document.open();
        PdfContentByte cb = writer.getDirectContent();
        for (Map.Entry<String, PDFCombinerFile> entry : pdfFileList.entrySet()) {
        	String pdfFileName = entry.getKey();
        	System.out.println("   key: " + pdfFileName);
        	InputStream in = new FileInputStream(pdfFileName);

        	// get pdf combiner file
            PDFCombinerFile pdfCombinerFile = pdfFileList.get(pdfFileName);

            // get reader
            PdfReader reader = new PdfReader(in);
            int numberOfPages = reader.getNumberOfPages();
            for (int i = 1; i <= numberOfPages; i++) {
                //import the page from source pdf
                PdfImportedPage page = writer.getImportedPage(reader, i);

                // set page size
                if(pdfCombinerFile != null && pdfCombinerFile.isProposalReport()) {
                	System.out.println("   setting proposal page size");
                	// if file is a proposal report, set a page size
                	float width = page.getWidth();
                	float height = page.getHeight();
                	document.setPageSize(new Rectangle(width, height));
                	//, scale, 0, 0, scale, x, y);
                    //add the page to the destination pdf
                    document.newPage();
                    float factor = .9f; // scale factor
                    float offsetY = (page.getHeight() - (page.getHeight() * factor));
                    System.out.println("      offsetY: " + offsetY);
                    float scaledWidth = page.getWidth() * factor;
                    float positionX = (page.getWidth() - scaledWidth) / 2;
                    System.out.println("      positionX: " + positionX);
                    cb.addTemplate(page, factor, 0, 0, factor, positionX, offsetY);
                } else {
                	document.setPageSize(page.getBoundingBox());
                	//add the page to the destination pdf
                	document.newPage();
                	cb.addTemplate(page, 0, 0);
                }

                // show title?
                if(pdfCombinerFile != null && pdfCombinerFile.isShowTitle()) {
                	document.add(new Paragraph(pdfCombinerFile.getTitle()));
                }
            }
        }

        outputStream.flush();
        document.close();
        outputStream.close();
    }

	private String getGeneratedCoverpagePdfPath(PDFCombinerArguments pdfCombinerArguments) throws IOException, Exception {
		String pdfTemplate = PDFS_TEMPLATES_DIR + File.separator + "coverpage.pdf";

        File tempFile = File.createTempFile("temp_", ".pdf", new File(this.getGeneratedPDFDir()));

        PdfReader pdfTemplatePdfReader = new PdfReader(pdfTemplate);
		FileOutputStream fileOutputStream = new FileOutputStream(tempFile);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		PdfStamper stamper = new PdfStamper(pdfTemplatePdfReader, fileOutputStream);
		stamper.setFormFlattening(true);

		stamper.getAcroFields().setField("proposalTitle", pdfCombinerArguments.getTitle());
		stamper.getAcroFields().setField("proposalSubtitle", pdfCombinerArguments.getSubTitle());
		stamper.getAcroFields().setField("clientCompanyName", pdfCombinerArguments.getClientCompanyName());
		stamper.getAcroFields().setField("clientContactInformation", pdfCombinerArguments.getClientContactInformation());
		stamper.getAcroFields().setField("agencyName", pdfCombinerArguments.getAgencyName());
		stamper.getAcroFields().setField("agencyContactInformation", pdfCombinerArguments.getAgencyContactInformation());
		stamper.getAcroFields().setField("marketName", pdfCombinerArguments.getMarketName());
		stamper.getAcroFields().setField("marketContactInformation", pdfCombinerArguments.getMarketContactInformation());
		stamper.getAcroFields().setField("versionNumber", pdfCombinerArguments.getVersionNumber());

		stamper.close();
		pdfTemplatePdfReader.close();

		System.out.println(tempFile.getAbsolutePath());

		return tempFile.getAbsolutePath();
	}

	// generate toc pdf
	private String getGeneratedTOCPdfPath(PDFCombinerArguments pdfCombinerArguments) throws IOException, Exception {
		String pdfTemplate = PDFS_TEMPLATES_DIR + File.separator + "table_of_contents.pdf";
        File tempFile = File.createTempFile("temp_", ".pdf", new File(this.getGeneratedPDFDir()));

        PdfReader pdfTemplatePdfReader = new PdfReader(pdfTemplate);
		FileOutputStream fileOutputStream = new FileOutputStream(tempFile);
		PdfStamper stamper = new PdfStamper(pdfTemplatePdfReader, fileOutputStream);
		stamper.setFormFlattening(true);

		if(pdfCombinerArguments.getPdfCombinerContentEntryList() != null && !pdfCombinerArguments.isExcludeFlightLines()) {
			int i = 0;

			for(PDFCombinerContentEntry pdfCombinerContentEntry : pdfCombinerArguments.getPdfCombinerContentEntryList()) {

				// set title
				String titleFieldName = String.format("ContentTitle%s", i + 1);
				stamper.getAcroFields().setField(titleFieldName, pdfCombinerContentEntry.getTitle());

				// set description
				String contentDescriptionFieldName = String.format("ContentDescription%s", i + 1);
				stamper.getAcroFields().setField(contentDescriptionFieldName, pdfCombinerContentEntry.getDescription());

				// set page number
				String pageNumberFieldName = String.format("ContentPage%s", i + 1);
				String pageNumber = String.valueOf(pdfCombinerContentEntry.getPageNumber());
				stamper.getAcroFields().setField(pageNumberFieldName, pageNumber);

				// for shipping instructions page override all field values
				if("Shipping Instructions".equals(pdfCombinerContentEntry.getTitle())) {
					stamper.getAcroFields().setField(titleFieldName,
						pdfCombinerContentEntry.getTitle() + "........................"	+ pageNumber);
					stamper.getAcroFields().setField(contentDescriptionFieldName, "");
					stamper.getAcroFields().setField(pageNumberFieldName, "");
				}

				i++;
			}
		}

		// determine starting page number...
		// if there are contents (proposal report), it will be the total pdf pages + 1 otherwise it will be 1.
		Integer startingPageNumber = pdfCombinerArguments.getContents() != null ? pdfCombinerArguments.getContents().get(0).getNumberOfPages() + 1 : 1;

		// refresh page numbers starting from the max page number (this number can be > 1 if there are content entries)
        pdfCombinerArguments.refreshPageNumbering(startingPageNumber);

		// set AppendixTitle fields
		if (pdfCombinerArguments.getAppendixes() != null) {
			// set appendix			
			if(!pdfCombinerArguments.getAppendixes().isEmpty())
				stamper.getAcroFields().setField(String.format("AppendixTitle%s", 1), "Appendix");
			int i = 1;
			for(PDFCombinerFile pdfCombinerFile : pdfCombinerArguments.getAppendixes()) {
				if(pdfCombinerFile.isShowAppendixEntry()) {

					// set appendix title
					String appendixTitleFieldName = String.format("AppendixTitle%s", i + 1);
					String pageTitle = pdfCombinerFile.getTitle();
					if(pageTitle != null && pageTitle.contains("_bis_sheet.pdf")){
						pageTitle = "BIS Sheets..........................................................................................";
					}
					if(pageTitle != null && pageTitle.contains("_maps.pdf")){
						pageTitle = "Maps................................................................................................";
					}
					stamper.getAcroFields().setField(appendixTitleFieldName, pageTitle);

					// set appendix page number
					String appendixPageFieldName = String.format("AppendixPage%s", i + 1);
					String startPageNumber = String.valueOf(pdfCombinerFile.getStartPageNumber());
					stamper.getAcroFields().setField(appendixPageFieldName, startPageNumber);

					// next index
					i++;
				}
			}
		}

		stamper.getAcroFields().setField("versionNumber", pdfCombinerArguments.getVersionNumber());

		stamper.close();
		pdfTemplatePdfReader.close();

		System.out.println(tempFile.getAbsolutePath());

		return tempFile.getAbsolutePath();
	}
}
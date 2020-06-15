package FileUploadORDownload;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
//import java.io.InputStream;
import java.io.OutputStream;
//import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import org.apache.pdfbox.pdmodel.PDDocument;

import org.springframework.stereotype.Service;
import java.util.Calendar;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.ExternalSigningSupport;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureOptions;


@Service
public class CreateSignature extends CreateSignatureBase {

	
	private static KeyStore keyStore;
	private static char[] pin;
	
	
	public CreateSignature() 
			throws UnrecoverableKeyException, KeyStoreException,
			NoSuchAlgorithmException, CertificateException, IOException {
		super();
}
	
	
public void SignPDF() {
		
		
		try {
			String keyStoreFilePath = "C:\\keystore.p12";
			keyStore = KeyStore.getInstance("PKCS12");
			String password = "123456";
			keyStore.load(new FileInputStream(keyStoreFilePath), password.toCharArray());
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	

	public void signDetached(File inFile, File outFile) throws IOException
	{
		signDetached(inFile, outFile, null);
	}
	
	public void signDetached(File inFile, File outFile, String tsaUrl) throws IOException
    {
        if (inFile == null || !inFile.exists())
        {
            throw new FileNotFoundException("Document for signing does not exist");
        }
        
        setTsaUrl(tsaUrl);

        // sign
        try (FileOutputStream fos = new FileOutputStream(outFile);
                PDDocument doc = PDDocument.load(inFile))
        {
            signDetached(doc, fos);
        }
    }
	
	public void signDetached(PDDocument document, OutputStream output)
            throws IOException
    {
        int accessPermissions = SigUtils.getMDPPermission(document);
        if (accessPermissions == 1)
        {
            throw new IllegalStateException("No changes to the document are permitted due to DocMDP transform parameters dictionary");
        }     

        // create signature dictionary
        PDSignature signature = new PDSignature();
        signature.setFilter(PDSignature.FILTER_ADOBE_PPKLITE);
        signature.setSubFilter(PDSignature.SUBFILTER_ADBE_PKCS7_DETACHED);
        signature.setName("Ravi Gupta");
        signature.setLocation("India");
        signature.setReason("Testing");
        // TODO extract the above details from the signing certificate? Reason as a parameter?

        // the signing date, needed for valid signature
        signature.setSignDate(Calendar.getInstance());

        // Optional: certify 
        if (accessPermissions == 0)
        {
            SigUtils.setMDPPermission(document, signature, 2);
        }        

        if (isExternalSigning())
        {
            document.addSignature(signature);
            ExternalSigningSupport externalSigning =
                    document.saveIncrementalForExternalSigning(output);
            // invoke external signature service
            byte[] cmsSignature = sign(externalSigning.getContent());
            // set signature bytes received from the service
            externalSigning.setSignature(cmsSignature);
        }
        else
        {
            SignatureOptions signatureOptions = new SignatureOptions();
            // Size can vary, but should be enough for purpose.
            signatureOptions.setPreferredSignatureSize(SignatureOptions.DEFAULT_SIGNATURE_SIZE * 2);
            // register signature dictionary and sign interface
            document.addSignature(signature, this, signatureOptions);

            // write incremental (only for signing purpose)
            document.saveIncremental(output);
        }
    }
//	
	public static KeyStore getKeyStore() {
		return keyStore;
	}

	public void setKeyStore(KeyStore keyStore) {
		CreateSignature.keyStore = keyStore;
	}

	public static  char[] getPin() {
		return pin;
	}

	public void setPin(char[] pin) {
		CreateSignature.pin = pin;
	}

	
	
	public File SignFile(File inFile) 
			throws UnrecoverableKeyException, KeyStoreException, 
			NoSuchAlgorithmException, CertificateException, IOException 
	{			
			boolean externalSig = false;
			String tsaUrl = null;
			
			
		 	setExternalSigning(externalSig);
		    String name = inFile.getName();
		    String substring = name.substring(0, name.lastIndexOf('.'));
		    
		    File outFile = new File(inFile.getParent(), substring + "_signed.pdf");
		    signDetached(inFile, outFile);
		    
		    return outFile;
	}
	
//	public static void main(String [] args) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, FileNotFoundException, IOException, UnrecoverableKeyException {
//		KeyStore keystore = KeyStore.getInstance("PKCS12");
//        char[] password = args[1].toCharArray(); // TODO use Java 6 java.io.Console.readPassword
//        keystore.load(new FileInputStream(args[0]), password);
//		 CreateSignature signing = new CreateSignature(keystore, password);
//		 File inFile = new File(args[2]);
//	        String name = inFile.getName();
//	        String substring = name.substring(0, name.lastIndexOf('.'));
//
//	        File outFile = new File(inFile.getParent(), substring + "_signed.pdf");
//	        signing.signDetached(inFile, outFile);
//	}

}

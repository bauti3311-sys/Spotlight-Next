import java.io.File;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.Collections;

/** Historical input check, no signing/export. --interactive prompts only on the local TTY. */
class Native60SignerPreflight {
    static final String EXPECTED = "885838aa970e0d4ba168a2b5f3e8f1a6e7334263c34c9cbc888f5c70381a52ba";
    static String fingerprint(KeyStore store, String alias) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(store.getCertificate(alias).getEncoded()));
    }
    public static void main(String[] args) {
        char[] storePass = null, keyPass = null;
        int exitCode = 0;
        try {
            boolean interactive = args.length == 2 && args[0].equals("--interactive");
            if (!interactive && args.length != 1) throw new IllegalArgumentException();
            var console = System.console();
            if (interactive && console == null) throw new IllegalArgumentException();
            storePass = interactive ? console.readPassword("Contraseña del almacén (sólo aquí, sin eco): ")
                : System.getenv("RAYCAST_PREVIEW_STORE_PASSWORD").toCharArray();
            if (storePass == null) throw new IllegalArgumentException();
            var store = KeyStore.getInstance(new File(args[interactive ? 1 : 0]), storePass);
            String alias;
            if (interactive) {
                var matches = new java.util.ArrayList<String>();
                for (String candidate : Collections.list(store.aliases())) {
                    if (store.isKeyEntry(candidate) && fingerprint(store, candidate).equals(EXPECTED)) matches.add(candidate);
                }
                if (matches.size() != 1) throw new IllegalArgumentException();
                alias = matches.get(0);
                keyPass = console.readPassword("Contraseña de la entrada (Enter = misma que almacén, sin eco): ");
            } else {
                alias = System.getenv("RAYCAST_PREVIEW_KEY_ALIAS");
                keyPass = System.getenv("RAYCAST_PREVIEW_KEY_PASSWORD").toCharArray();
            }
            if (keyPass != null && interactive && keyPass.length == 0) keyPass = storePass.clone();
            if (keyPass == null || !(store.getKey(alias, keyPass) instanceof PrivateKey)) throw new IllegalArgumentException();
            if (!fingerprint(store, alias).equals(EXPECTED)) throw new IllegalArgumentException();
            System.out.println("Firmador histórico Preview verificado: certificado 885838aa… y clave accesible. Ninguna clave exportada; ninguna APK firmada.");
        } catch (Exception error) {
            System.err.println("No se pudo validar el firmador histórico. No se muestran datos privados.");
            exitCode = 1;
        } finally {
            if (storePass != null) Arrays.fill(storePass, '\0');
            if (keyPass != null) Arrays.fill(keyPass, '\0');
        }
        if (exitCode != 0) System.exit(exitCode);
    }
}

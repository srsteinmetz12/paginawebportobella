package util;
import geradorlicencacliente.LicencaManager;
import geradorlicencacliente.LicencaVO;
import java.io.File;

public class LeituraLicenca {

    public static void main(String[] args) {
        // Altere o caminho se salvou o arquivo em outra pasta (ex: "config/licenca.lic")
        String caminhoLicenca = "licenca.lic"; 

        // 1. Verifica primeiro se o arquivo sequer existe na pasta
        File arquivo = new File(caminhoLicenca);
        if (!arquivo.exists()) {
            MensagemSistema.mostrarAvisoDark(null, 
                "Arquivo licenca.lic não foi encontrado na pasta do sistema!");
            return;
        }

        try {
            // 2. Tenta ler e descriptografar a licença
            LicencaVO licenca = LicencaManager.lerArquivoLicenca(caminhoLicenca);

            // 3. Valida se está expirada
            if (licenca.estaExpirada()) {
                MensagemSistema.mostrarAvisoDark(null, 
                    "Bloqueado! Esta licença expirou em: " + licenca.getDataExpiracao());
                return;
            }

            // 4. Sucesso! Mostra os dados recuperados de dentro do arquivo criptografado
            String mensagemSucesso = 
                    "  <b style='color: #D4AF37;'>=== LICENÇA VÁLIDA RECONHECIDA ===</b><br><br>" +
                    "  <b>Cliente:</b> " + licenca.getDocumentoCliente() + "<br>" +
                    "  <b>Módulo Liberado:</b> " + licenca.getTipoArmazenamento() + "<br>" +
                    "  <b>Válido por mais:</b> " + licenca.getDiasRestantes() + " dias<br>" +
                    "  <b>Data de Expiração:</b> " + licenca.getDataExpiracao();

            MensagemSistema.mostrarAvisoDark(null, mensagemSucesso);

            // 5. Aqui você integraria com a Factory que criamos anteriormente:
            // TipoArmazenamento modoCliente = licenca.getTipoArmazenamento();
            // UsuarioDao dao = DaoFactory.getUsuarioDao(modoCliente);

        } catch (Exception e) {
            // Se o cliente alterou uma única letra no arquivo, vai cair aqui
            MensagemSistema.mostrarAvisoDark(null, 
                "Erro: O arquivo de licença está corrompido ou a chave AES é inválida!\nMensagem: " + e.getMessage());
        }
    }
}

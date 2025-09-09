package NFSe_manual;

public class ModeloNFSe {
    private String numero;
    private String codigoVerificacao;
    private String dataEmissao;
    private String prestador;
    private String tomador;
    private double valorTotal;

    public ModeloNFSe() {
    }

    public ModeloNFSe(String numero, String codigoVerificacao, String dataEmissao, String prestador, String tomador, double valorTotal) {
        this.numero = numero;
        this.codigoVerificacao = codigoVerificacao;
        this.dataEmissao = dataEmissao;
        this.prestador = prestador;
        this.tomador = tomador;
        this.valorTotal = valorTotal;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getCodigoVerificacao() {
        return codigoVerificacao;
    }

    public void setCodigoVerificacao(String codigoVerificacao) {
        this.codigoVerificacao = codigoVerificacao;
    }

    public String getDataEmissao() {
        return dataEmissao;
    }

    public void setDataEmissao(String dataEmissao) {
        this.dataEmissao = dataEmissao;
    }

    public String getPrestador() {
        return prestador;
    }

    public void setPrestador(String prestador) {
        this.prestador = prestador;
    }

    public String getTomador() {
        return tomador;
    }

    public void setTomador(String tomador) {
        this.tomador = tomador;
    }

    public double getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(double valorTotal) {
        this.valorTotal = valorTotal;
    }

    @Override
    public String toString() {
        return "ModeloNFSe{" +
                "numero='" + numero + '\'' +
                ", codigoVerificacao='" + codigoVerificacao + '\'' +
                ", dataEmissao='" + dataEmissao + '\'' +
                ", prestador='" + prestador + '\'' +
                ", tomador='" + tomador + '\'' +
                ", valorTotal=" + valorTotal +
                '}';
    }
}
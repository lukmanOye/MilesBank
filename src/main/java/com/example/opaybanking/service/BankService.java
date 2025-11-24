package com.example.opaybanking.service;

import com.example.opaybanking.model.Bank;
import com.example.opaybanking.repo.BankRepo;
import jakarta.annotation.PostConstruct;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class BankService {

    private static final Logger log = LoggerFactory.getLogger(BankService.class);
    private final BankRepo bankRepo;
    private final RestTemplate rest = new RestTemplate();

    @Value("${paystack.secret-key}")
    private String paystackSecretKey;

    public BankService(BankRepo bankRepo) {
        this.bankRepo = bankRepo;
    }

    @PostConstruct
    public void init() {
        try {
            if (bankRepo.count() == 0) {
                log.info("Initializing banks in database...");
                initializeBanksInDatabase();
            } else {
                log.info("Banks already initialized in database. Count: {}", bankRepo.count());
            }
        } catch (Exception e) {
            log.error("Error during bank initialization: {}", e.getMessage());
        }
    }

    private void initializeBanksInDatabase() {
        try {
            List<Bank> banksToSave = new ArrayList<>();
            Set<String> existingBankCodes = new HashSet<>();

            List<Bank> paystackBanks = fetchBanksFromPaystack();
            for (Bank bank : paystackBanks) {
                if (!existingBankCodes.contains(bank.getBankCode())) {
                    banksToSave.add(bank);
                    existingBankCodes.add(bank.getBankCode());
                }
            }


            if (banksToSave.isEmpty()) {
                banksToSave = getUniqueHardcodedBanks();
            } else {

                Bank milesBank = new Bank("190909", "Miles Bank", null);
                if (!existingBankCodes.contains(milesBank.getBankCode())) {
                    banksToSave.add(milesBank);
                }
            }

            int batchSize = 50;
            for (int i = 0; i < banksToSave.size(); i += batchSize) {
                List<Bank> batch = banksToSave.subList(i, Math.min(i + batchSize, banksToSave.size()));
                try {
                    bankRepo.saveAll(batch);
                    log.info("Saved batch of {} banks", batch.size());
                } catch (Exception e) {
                    log.warn("Failed to save batch, trying individual saves: {}", e.getMessage());
                    saveBanksIndividually(batch);
                }
            }

            log.info("Successfully loaded {} banks into database", banksToSave.size());

        } catch (Exception e) {
            log.error("Failed to initialize banks, using hardcoded fallback", e);
            saveBanksIndividually(getUniqueHardcodedBanks());
        }
    }

    private void saveBanksIndividually(List<Bank> banks) {
        int savedCount = 0;
        for (Bank bank : banks) {
            try {
                if (!bankRepo.existsByBankCode(bank.getBankCode())) {
                    bankRepo.save(bank);
                    savedCount++;
                }
            } catch (Exception e) {
                log.warn("Failed to save bank {} ({}): {}", bank.getBankCode(), bank.getBankName(), e.getMessage());
            }
        }
        log.info("Individually saved {} banks", savedCount);
    }

    private List<Bank> fetchBanksFromPaystack() {
        try {
            String url = "https://api.paystack.co/bank?country=nigeria&perPage=500";
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + paystackSecretKey);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<Map> response = rest.exchange(url, HttpMethod.GET, entity, Map.class);

            Map<String, Object> body = response.getBody();
            if (body != null && Boolean.TRUE.equals(body.get("status"))) {
                List<Map<String, Object>> banksData = (List<Map<String, Object>>) body.get("data");
                List<Bank> banks = new ArrayList<>();

                for (Map<String, Object> bankData : banksData) {
                    String code = String.valueOf(bankData.get("code"));
                    String name = String.valueOf(bankData.get("name"));
                    banks.add(new Bank(code, name, null));
                }
                return banks;
            }
        } catch (Exception ex) {
            log.error("Error fetching banks from Paystack: {}", ex.getMessage());
        }
        return new ArrayList<>();
    }

    private List<Bank> getUniqueHardcodedBanks() {
        Map<String, Bank> uniqueBanks = new LinkedHashMap<>();


        addBank(uniqueBanks, "120001", "9mobile 9Payment Service Bank");
        addBank(uniqueBanks, "404", "Abbey Mortgage Bank");
        addBank(uniqueBanks, "51204", "Above Only MFB");
        addBank(uniqueBanks, "51312", "Abulesoro MFB");
        addBank(uniqueBanks, "044", "Access Bank");
        addBank(uniqueBanks, "063", "Access Bank (Diamond)");
        addBank(uniqueBanks, "602", "Accion Microfinance Bank");
        addBank(uniqueBanks, "50315", "Aella MFB");
        addBank(uniqueBanks, "90077", "AG Mortgage Bank");
        addBank(uniqueBanks, "50036", "Ahmadu Bello University Microfinance Bank");
        addBank(uniqueBanks, "120004", "Airtel Smartcash PSB");
        addBank(uniqueBanks, "51336", "AKU Microfinance Bank");
        addBank(uniqueBanks, "090561", "Akuchukwu Microfinance Bank Limited");
        addBank(uniqueBanks, "50055", "Al-Barakah Microfinance Bank");
        addBank(uniqueBanks, "035A", "ALAT by WEMA");
        addBank(uniqueBanks, "108", "Alpha Morgan Bank");
        addBank(uniqueBanks, "000304", "Alternative bank");
        addBank(uniqueBanks, "090629", "Amegy Microfinance Bank");
        addBank(uniqueBanks, "50926", "Amju Unique MFB");
        addBank(uniqueBanks, "50083", "Aramoko MFB");
        addBank(uniqueBanks, "401", "ASO Savings and Loans");
        addBank(uniqueBanks, "50092", "Assets Microfinance Bank");
        addBank(uniqueBanks, "MFB50094", "Astrapolaris MFB LTD");
        addBank(uniqueBanks, "090478", "AVUENEGBE MICROFINANCE BANK");
        addBank(uniqueBanks, "51351", "AWACASH MICROFINANCE BANK");
        addBank(uniqueBanks, "51337", "AZTEC MICROFINANCE BANK LIMITED");
        addBank(uniqueBanks, "51229", "Bainescredit MFB");
        addBank(uniqueBanks, "50117", "Banc Corp Microfinance Bank");
        addBank(uniqueBanks, "50572", "BANKIT MICROFINANCE BANK LTD");
        addBank(uniqueBanks, "51341", "BANKLY MFB");
        addBank(uniqueBanks, "MFB50992", "Baobab Microfinance Bank");
        addBank(uniqueBanks, "51100", "BellBank Microfinance Bank");
        addBank(uniqueBanks, "51267", "Benysta Microfinance Bank Limited");
        addBank(uniqueBanks, "50123", "Beststar Microfinance Bank");
        addBank(uniqueBanks, "50725", "BOLD MFB");
        addBank(uniqueBanks, "650", "Bosak Microfinance Bank");
        addBank(uniqueBanks, "50931", "Bowen Microfinance Bank");
        addBank(uniqueBanks, "FC40163", "Branch International Finance Company Limited");
        addBank(uniqueBanks, "90070", "Brent Mortgage bank");
        addBank(uniqueBanks, "50645", "BuyPower MFB");
        addBank(uniqueBanks, "565", "Carbon");
        addBank(uniqueBanks, "51353", "Cashbridge Microfinance Bank Limited");
        addBank(uniqueBanks, "865", "CASHCONNECT MFB");
        addBank(uniqueBanks, "50823", "CEMCS Microfinance Bank");
        addBank(uniqueBanks, "50171", "Chanelle Microfinance Bank Limited");
        addBank(uniqueBanks, "312", "Chikum Microfinance bank");
        addBank(uniqueBanks, "023", "Citibank Nigeria");
        addBank(uniqueBanks, "070027", "CITYCODE MORTAGE BANK");
        addBank(uniqueBanks, "50910", "Consumer Microfinance Bank");
        addBank(uniqueBanks, "51458", "Cool Microfinance Bank Limited");
        addBank(uniqueBanks, "50204", "Corestep MFB");
        addBank(uniqueBanks, "559", "Coronation Merchant Bank");
        addBank(uniqueBanks, "FC40128", "County Finance Limited");
        addBank(uniqueBanks, "40119", "Credit Direct Limited");
        addBank(uniqueBanks, "51297", "Crescent MFB");
        addBank(uniqueBanks, "090560", "Crust Microfinance Bank");
        addBank(uniqueBanks, "50216", "CRUTECH MICROFINANCE BANK LTD");
        addBank(uniqueBanks, "51368", "Dash Microfinance Bank");
        addBank(uniqueBanks, "51334", "Davenport MICROFINANCE BANK");
        addBank(uniqueBanks, "51450", "Dillon Microfinance Bank");
        addBank(uniqueBanks, "50162", "Dot Microfinance Bank");
        addBank(uniqueBanks, "50922", "EBSU Microfinance Bank");
        addBank(uniqueBanks, "050", "Ecobank Nigeria");
        addBank(uniqueBanks, "50263", "Ekimogun MFB");
        addBank(uniqueBanks, "098", "Ekondo Microfinance Bank");
        addBank(uniqueBanks, "090678", "EXCEL FINANCE BANK");
        addBank(uniqueBanks, "50126", "Eyowo");
        addBank(uniqueBanks, "51318", "Fairmoney Microfinance Bank");
        addBank(uniqueBanks, "50298", "Fedeth MFB");
        addBank(uniqueBanks, "070", "Fidelity Bank");
        addBank(uniqueBanks, "51314", "Firmus MFB");
        addBank(uniqueBanks, "011", "First Bank of Nigeria");
        addBank(uniqueBanks, "214", "First City Monument Bank");
        addBank(uniqueBanks, "090164", "FIRST ROYAL MICROFINANCE BANK");
        addBank(uniqueBanks, "51333", "FIRSTMIDAS MFB");
        addBank(uniqueBanks, "413", "FirstTrust Mortgage Bank Nigeria");
        addBank(uniqueBanks, "501", "FSDH Merchant Bank Limited");
        addBank(uniqueBanks, "832", "FUTMINNA MICROFINANCE BANK");
        addBank(uniqueBanks, "MFB51093", "Garun Mallam MFB");
        addBank(uniqueBanks, "812", "Gateway Mortgage Bank LTD");
        addBank(uniqueBanks, "00103", "Globus Bank");
        addBank(uniqueBanks, "090574", "Goldman MFB");
        addBank(uniqueBanks, "100022", "GoMoney");
        addBank(uniqueBanks, "090664", "GOOD SHEPHERD MICROFINANCE BANK");
        addBank(uniqueBanks, "50739", "Goodnews Microfinance Bank");
        addBank(uniqueBanks, "562", "Greenwich Merchant Bank");
        addBank(uniqueBanks, "51276", "GROOMING MICROFINANCE BANK");
        addBank(uniqueBanks, "50368", "GTI MFB");
        addBank(uniqueBanks, "058", "Guaranty Trust Bank");
        addBank(uniqueBanks, "51251", "Hackman Microfinance Bank");
        addBank(uniqueBanks, "50383", "Hasal Microfinance Bank");
        addBank(uniqueBanks, "51364", "Hayat Trust MFB");
        addBank(uniqueBanks, "120002", "HopePSB");
        addBank(uniqueBanks, "51211", "IBANK Microfinance Bank");
        addBank(uniqueBanks, "51279", "IBBU MFB");
        addBank(uniqueBanks, "51244", "Ibile Microfinance Bank");
        addBank(uniqueBanks, "90012", "Ibom Mortgage Bank");
        addBank(uniqueBanks, "50439", "Ikoyi Osun MFB");
        addBank(uniqueBanks, "50442", "Ilaro Poly Microfinance Bank");
        addBank(uniqueBanks, "50453", "Imowo MFB");
        addBank(uniqueBanks, "415", "IMPERIAL HOMES MORTAGE BANK");
        addBank(uniqueBanks, "51392", "INDULGE MFB");
        addBank(uniqueBanks, "50457", "Infinity MFB");
        addBank(uniqueBanks, "070016", "Infinity trust Mortgage Bank");
        addBank(uniqueBanks, "090701", "ISUA MFB");
        addBank(uniqueBanks, "301", "Jaiz Bank");
        addBank(uniqueBanks, "50502", "Kadpoly MFB");
        addBank(uniqueBanks, "51308", "KANOPOLY MFB");
        addBank(uniqueBanks, "5129", "Kayvee Microfinance Bank");
        addBank(uniqueBanks, "082", "Keystone Bank");
        addBank(uniqueBanks, "899", "Kolomoni MFB");
        addBank(uniqueBanks, "100025", "KONGAPAY (Kongapay Technologies Limited)(formerly Zinternet)");
        addBank(uniqueBanks, "50200", "Kredi Money MFB LTD");
        addBank(uniqueBanks, "50211", "Kuda Bank");
        addBank(uniqueBanks, "90052", "Lagos Building Investment Company Plc.");
        addBank(uniqueBanks, "090420", "Letshego Microfinance Bank");
        addBank(uniqueBanks, "50549", "Links MFB");
        addBank(uniqueBanks, "031", "Living Trust Mortgage Bank");
        addBank(uniqueBanks, "50491", "LOMA MFB");
        addBank(uniqueBanks, "303", "Lotus Bank");
        addBank(uniqueBanks, "51444", "Maal MFB");
        addBank(uniqueBanks, "090171", "MAINSTREET MICROFINANCE BANK");
        addBank(uniqueBanks, "50563", "Mayfair MFB");
        addBank(uniqueBanks, "50304", "Mint MFB");
        addBank(uniqueBanks, "09", "MINT-FINEX MFB");
        addBank(uniqueBanks, "946", "Money Master PSB");
        addBank(uniqueBanks, "50515", "Moniepoint MFB");
        addBank(uniqueBanks, "120003", "MTN Momo PSB");
        addBank(uniqueBanks, "090190", "MUTUAL BENEFITS MICROFINANCE BANK");
        addBank(uniqueBanks, "090679", "NDCC MICROFINANCE BANK");
        addBank(uniqueBanks, "51361", "NET MICROFINANCE BANK");
        addBank(uniqueBanks, "51142", "Nigerian Navy Microfinance Bank Limited");
        addBank(uniqueBanks, "50072", "Nombank MFB");
        addBank(uniqueBanks, "561", "NOVA BANK");
        addBank(uniqueBanks, "51371", "Novus MFB");
        addBank(uniqueBanks, "50629", "NPF MICROFINANCE BANK");
        addBank(uniqueBanks, "51261", "NSUK MICROFINANACE BANK");
        addBank(uniqueBanks, "50689", "Olabisi Onabanjo University Microfinance Bank");
        addBank(uniqueBanks, "50697", "OLUCHUKWU MICROFINANCE BANK LTD");
        addBank(uniqueBanks, "999992", "OPay Digital Services Limited (OPay)");
        addBank(uniqueBanks, "107", "Optimus Bank Limited");
        addBank(uniqueBanks, "100002", "Paga");
        addBank(uniqueBanks, "999991", "PalmPay");
        addBank(uniqueBanks, "104", "Parallex Bank");
        addBank(uniqueBanks, "311", "Parkway - ReadyCash");
        addBank(uniqueBanks, "090680", "PATHFINDER MICROFINANCE BANK LIMITED");
        addBank(uniqueBanks, "100039", "Paystack-Titan");
        addBank(uniqueBanks, "50743", "Peace Microfinance Bank");
        addBank(uniqueBanks, "51226", "PECANTRUST MICROFINANCE BANK LIMITED");
        addBank(uniqueBanks, "51146", "Personal Trust MFB");
        addBank(uniqueBanks, "50746", "Petra Mircofinance Bank Plc");
        addBank(uniqueBanks, "MFB51452", "Pettysave MFB");
        addBank(uniqueBanks, "050021", "PFI FINANCE COMPANY LIMITED");
        addBank(uniqueBanks, "268", "Platinum Mortgage Bank");
        addBank(uniqueBanks, "00716", "Pocket App");
        addBank(uniqueBanks, "076", "Polaris Bank");
        addBank(uniqueBanks, "50864", "Polyunwana MFB");
        addBank(uniqueBanks, "105", "PremiumTrust Bank");
        addBank(uniqueBanks, "050023", "PROSPERIS FINANCE LIMITED");
        addBank(uniqueBanks, "101", "Providus Bank");
        addBank(uniqueBanks, "51293", "QuickFund MFB");
        addBank(uniqueBanks, "502", "Rand Merchant Bank");
        addBank(uniqueBanks, "090496", "RANDALPHA MICROFINANCE BANK");
        addBank(uniqueBanks, "90067", "Refuge Mortgage Bank");
        addBank(uniqueBanks, "50761", "REHOBOTH MICROFINANCE BANK");
        addBank(uniqueBanks, "50994", "Rephidim Microfinance Bank");
        addBank(uniqueBanks, "51286", "Rigo Microfinance Bank Limited");
        addBank(uniqueBanks, "50767", "ROCKSHIELD MICROFINANCE BANK");
        addBank(uniqueBanks, "125", "Rubies MFB");
        addBank(uniqueBanks, "51113", "Safe Haven MFB");
        addBank(uniqueBanks, "40165", "SAGE GREY FINANCE LIMITED");
        addBank(uniqueBanks, "50582", "Shield MFB");
        addBank(uniqueBanks, "106", "Signature Bank Ltd");
        addBank(uniqueBanks, "51062", "Solid Allianze MFB");
        addBank(uniqueBanks, "50800", "Solid Rock MFB");
        addBank(uniqueBanks, "51310", "Sparkle Microfinance Bank");
        addBank(uniqueBanks, "51429", "Springfield Microfinance Bank");
        addBank(uniqueBanks, "221", "Stanbic IBTC Bank");
        addBank(uniqueBanks, "068", "Standard Chartered Bank");
        addBank(uniqueBanks, "090162", "STANFORD MICROFINANCE BANK");
        addBank(uniqueBanks, "50809", "STATESIDE MICROFINANCE BANK");
        addBank(uniqueBanks, "070022", "STB Mortgage Bank");
        addBank(uniqueBanks, "51253", "Stellas MFB");
        addBank(uniqueBanks, "232", "Sterling Bank");
        addBank(uniqueBanks, "00305", "Summit Bank");
        addBank(uniqueBanks, "100", "Suntrust Bank");
        addBank(uniqueBanks, "50968", "Supreme MFB");
        addBank(uniqueBanks, "302", "TAJ Bank");
        addBank(uniqueBanks, "51269", "Tangerine Money");
        addBank(uniqueBanks, "51403", "TENN");
        addBank(uniqueBanks, "677", "Think Finance Microfinance Bank");
        addBank(uniqueBanks, "102", "Titan Bank");
        addBank(uniqueBanks, "090708", "TransPay MFB");
        addBank(uniqueBanks, "51118", "TRUSTBANC J6 MICROFINANCE BANK");
        addBank(uniqueBanks, "50840", "U&C Microfinance Bank Ltd (U AND C MFB)");
        addBank(uniqueBanks, "090706", "UCEE MFB");
        addBank(uniqueBanks, "51322", "Uhuru MFB");
        addBank(uniqueBanks, "51080", "Ultraviolet Microfinance Bank");
        addBank(uniqueBanks, "50870", "Unaab Microfinance Bank Limited");
        addBank(uniqueBanks, "51447", "UNIABUJA MFB");
        addBank(uniqueBanks, "50871", "Unical MFB");
        addBank(uniqueBanks, "51316", "Unilag Microfinance Bank");
        addBank(uniqueBanks, "50875", "UNIMAID MICROFINANCE BANK");
        addBank(uniqueBanks, "032", "Union Bank of Nigeria");
        addBank(uniqueBanks, "033", "United Bank For Africa");
        addBank(uniqueBanks, "215", "Unity Bank");
        addBank(uniqueBanks, "50894", "Uzondu Microfinance Bank Awka Anambra State");
        addBank(uniqueBanks, "050020", "Vale Finance Limited");
        addBank(uniqueBanks, "566", "VFD Microfinance Bank Limited");
        addBank(uniqueBanks, "51355", "Waya Microfinance Bank");
        addBank(uniqueBanks, "035", "Wema Bank");
        addBank(uniqueBanks, "51386", "Weston Charis MFB");
        addBank(uniqueBanks, "100040", "Xpress Wallet");
        addBank(uniqueBanks, "594", "Yes MFB");
        addBank(uniqueBanks, "00zap", "Zap");
        addBank(uniqueBanks, "057", "Zenith Bank");
        addBank(uniqueBanks, "51373", "Zitra MFB");
        addBank(uniqueBanks, "190909", "Miles Bank");

        return new ArrayList<>(uniqueBanks.values());
    }

    private void addBank(Map<String, Bank> bankMap, String code, String name) {
        if (!bankMap.containsKey(code)) {
            bankMap.put(code, new Bank(code, name, null));
        } else {
            log.warn("Duplicate bank code skipped: {} for bank: {}", code, name);
        }
    }

    public String resolveBankName(String bankCode) {
        return bankRepo.findByBankCode(bankCode)
                .map(Bank::getBankName)
                .orElse("Unknown Bank");
    }

    public List<Bank> getAllBanks() {
        return bankRepo.findAllByOrderByBankNameAsc();
    }

    public Optional<Bank> findByCode(String bankCode) {
        return bankRepo.findByBankCode(bankCode);
    }

    public boolean isValidBankCode(String bankCode) {
        return bankRepo.findByBankCode(bankCode).isPresent();
    }
}
// Équivalent C++ de StaxLegacyParser (DOM + XPath, via pugixml), avec le même modèle de données.
//
// Usage :
//   camt_bench dump  <fichier.xml>
//   camt_bench bench <fichier.xml> [fenêtres_warmup=3] [secondes_warmup=5] [fenêtres_mesure=3] [secondes_mesure=10]
//
// pugixml ne traite pas les namespaces : comme le DocumentBuilderFactory Java par défaut,
// "//Ntry" trouve les éléments du namespace par défaut du fichier.

#include "third_party/pugixml/pugixml.hpp"

#include <charconv>
#include <chrono>
#include <cstdint>
#include <cstdio>
#include <cstdlib>
#include <fstream>
#include <iostream>
#include <iterator>
#include <optional>
#include <sstream>
#include <stdexcept>
#include <string>
#include <vector>

// modèle

// Équivalent de MonetaryAmountLegacy : BigDecimal -> (entier, échelle), Currency -> chaîne.
struct Amount {
    int64_t unscaled;
    unsigned scale;
    std::string currency;
};

enum class CreditDebit { Crdt, Dbit };
enum class Status { Book, Pdng, Info };

struct Date {
    int year;
    unsigned month;
    unsigned day;
};

struct Parties {
    std::optional<std::string> debtorName;
    std::optional<std::string> debtorIban;
    std::optional<std::string> creditorName;
    std::optional<std::string> creditorIban;
};

struct EntryDetails {
    std::optional<std::string> endToEndId;
    std::optional<Amount> txAmount;
    std::optional<Parties> parties;
    std::optional<std::vector<std::string>> remittance;
};

struct Transaction {
    Amount amount;
    CreditDebit creditDebit;
    Status status;
    std::optional<Date> bookingDate;
    std::optional<std::vector<EntryDetails>> details;
};

// conversions

template <class T>
static T parse_int(const std::string& s, const char* what) {
    T value{};
    auto [ptr, ec] = std::from_chars(s.data(), s.data() + s.size(), value);
    if (s.empty() || ec != std::errc() || ptr != s.data() + s.size()) {
        throw std::runtime_error(std::string(what) + " invalide : \"" + s + "\"");
    }
    return value;
}

static Amount parse_amount(const std::string& text, const std::string& currency) {
    std::string intPart = text;
    std::string fracPart;
    if (auto dot = text.find('.'); dot != std::string::npos) {
        intPart = text.substr(0, dot);
        fracPart = text.substr(dot + 1);
    }
    return Amount{parse_int<int64_t>(intPart + fracPart, "montant"),
                  static_cast<unsigned>(fracPart.size()), currency};
}

static CreditDebit parse_credit_debit(const std::string& s) {
    if (s == "CRDT") return CreditDebit::Crdt;
    if (s == "DBIT") return CreditDebit::Dbit;
    throw std::runtime_error("CdtDbtInd invalide : \"" + s + "\"");
}

static Status parse_status(const std::string& s) {
    if (s == "BOOK") return Status::Book;
    if (s == "PDNG") return Status::Pdng;
    if (s == "INFO") return Status::Info;
    throw std::runtime_error("Sts invalide : \"" + s + "\"");
}

static Date parse_date(const std::string& s) {
    auto d1 = s.find('-');
    auto d2 = d1 == std::string::npos ? d1 : s.find('-', d1 + 1);
    if (d1 == std::string::npos || d2 == std::string::npos || s.find('-', d2 + 1) != std::string::npos) {
        throw std::runtime_error("date invalide : \"" + s + "\"");
    }
    return Date{parse_int<int>(s.substr(0, d1), "année"),
                parse_int<unsigned>(s.substr(d1 + 1, d2 - d1 - 1), "mois"),
                parse_int<unsigned>(s.substr(d2 + 1), "jour")};
}

// XPath

// Chaque appel compile l'expression, comme xpath.evaluate(String, ...) en Java.
static std::string eval_string(const pugi::xml_node& node, const char* expr) {
    pugi::xpath_query query(expr);
    return query.evaluate_string(node);
}

static pugi::xml_node eval_node(const pugi::xml_node& node, const char* expr) {
    return node.select_node(expr).node();
}

static std::optional<std::string> non_empty(std::string s) {
    if (s.empty()) return std::nullopt;
    return s;
}

//   parseur

static EntryDetails parse_transaction_details(const pugi::xml_node& tx) {
    EntryDetails d;
    d.endToEndId = non_empty(eval_string(tx, "./Refs/EndToEndId"));

    if (pugi::xml_node n = eval_node(tx, "./AmtDtls/TxAmt/Amt")) {
        d.txAmount = parse_amount(n.text().get(), n.attribute("Ccy").as_string());
    }

    Parties p;
    p.debtorName = non_empty(eval_string(tx, "./RltdPties/Dbtr/Nm"));
    p.debtorIban = non_empty(eval_string(tx, "./RltdPties/DbtrAcct/Id/IBAN"));
    p.creditorName = non_empty(eval_string(tx, "./RltdPties/Cdtr/Nm"));
    p.creditorIban = non_empty(eval_string(tx, "./RltdPties/CdtrAcct/Id/IBAN"));
    if (p.debtorName || p.debtorIban || p.creditorName || p.creditorIban) {
        d.parties = std::move(p);
    }

    if (auto remittance = non_empty(eval_string(tx, "./RmtInf/Ustrd"))) {
        d.remittance = std::vector<std::string>{*remittance};
    }
    return d;
}

static std::vector<EntryDetails> parse_entry_details(const pugi::xml_node& ntryDtls) {
    std::vector<EntryDetails> out;
    pugi::xpath_node_set txNodes = ntryDtls.select_nodes("./TxDtls");
    out.reserve(txNodes.size());
    for (const pugi::xpath_node& tx : txNodes) {
        out.push_back(parse_transaction_details(tx.node()));
    }
    return out;
}

static Transaction parse_transaction(const pugi::xml_node& ntry) {
    pugi::xml_node amtNode = eval_node(ntry, "./Amt");
    if (!amtNode) throw std::runtime_error("Amt absent");
    Amount amount = parse_amount(eval_string(ntry, "./Amt"), amtNode.attribute("Ccy").as_string());

    CreditDebit cd = parse_credit_debit(eval_string(ntry, "./CdtDbtInd"));
    Status status = parse_status(eval_string(ntry, "./Sts"));

    std::optional<Date> bookingDate;
    if (std::string bookDate = eval_string(ntry, "./BookgDt/Dt"); !bookDate.empty()) {
        bookingDate = parse_date(bookDate);
    }

    std::optional<std::vector<EntryDetails>> details;
    if (pugi::xml_node ntryDtls = eval_node(ntry, "./NtryDtls")) {
        details = parse_entry_details(ntryDtls);
    }

    return Transaction{std::move(amount), cd, status, bookingDate, std::move(details)};
}

static std::vector<Transaction> parse(const std::string& xml) {
    pugi::xml_document doc;
    pugi::xml_parse_result result = doc.load_buffer(xml.data(), xml.size());
    if (!result) {
        throw std::runtime_error(std::string("XML invalide : ") + result.description());
    }
    pugi::xpath_node_set ntryNodes = doc.select_nodes("//Ntry");
    std::vector<Transaction> transactions;
    transactions.reserve(ntryNodes.size());
    for (const pugi::xpath_node& ntry : ntryNodes) {
        transactions.push_back(parse_transaction(ntry.node()));
    }
    return transactions;
}

// dump

static std::string fmt_amount(const std::optional<Amount>& a) {
    if (!a) return "null";
    bool neg = a->unscaled < 0;
    uint64_t abs = neg ? 0 - static_cast<uint64_t>(a->unscaled) : static_cast<uint64_t>(a->unscaled);
    std::string digits = std::to_string(abs);
    size_t scale = a->scale;
    if (scale > 0) {
        while (digits.size() <= scale) digits.insert(digits.begin(), '0');
        digits.insert(digits.size() - scale, ".");
    }
    return std::string(neg ? "-" : "") + digits + " " + a->currency;
}

static std::string opt(const std::optional<std::string>& s) { return s ? *s : "null"; }

static std::string join(const std::vector<std::string>& v) {
    std::string out;
    for (size_t i = 0; i < v.size(); ++i) {
        if (i) out += "|";
        out += v[i];
    }
    return out;
}

static std::string dump(const std::vector<Transaction>& txs) {
    std::ostringstream out;
    out << "entries=" << txs.size() << "\n";
    for (size_t i = 0; i < txs.size(); ++i) {
        const Transaction& t = txs[i];
        char date[16] = "null";
        if (t.bookingDate) {
            std::snprintf(date, sizeof date, "%04d-%02u-%02u", t.bookingDate->year,
                          t.bookingDate->month, t.bookingDate->day);
        }
        out << "[" << i << "] amt=" << fmt_amount(t.amount)
            << " cd=" << (t.creditDebit == CreditDebit::Crdt ? "CRDT" : "DBIT") << " sts="
            << (t.status == Status::Book ? "BOOK" : t.status == Status::Pdng ? "PDNG" : "INFO")
            << " date=" << date << " txdtls="
            << (t.details ? std::to_string(t.details->size()) : std::string("null")) << "\n";
        if (!t.details) continue;
        for (size_t j = 0; j < t.details->size(); ++j) {
            const EntryDetails& d = (*t.details)[j];
            static const std::optional<std::string> none;
            const Parties* p = d.parties ? &*d.parties : nullptr;
            out << "[" << i << "." << j << "] e2e=" << opt(d.endToEndId)
                << " txamt=" << fmt_amount(d.txAmount)
                << " dbtr=" << opt(p ? p->debtorName : none)
                << " dbtrIban=" << opt(p ? p->debtorIban : none)
                << " cdtr=" << opt(p ? p->creditorName : none)
                << " cdtrIban=" << opt(p ? p->creditorIban : none)
                << " rmt=" << (d.remittance ? join(*d.remittance) : std::string("null")) << "\n";
        }
    }
    return out.str();
}

// benchmark

static std::string read_file(const std::string& path) {
    std::ifstream in(path, std::ios::binary);
    if (!in) throw std::runtime_error("lecture impossible : " + path);
    return std::string(std::istreambuf_iterator<char>(in), std::istreambuf_iterator<char>());
}

// Équivalent de Blackhole.consume / std::hint::black_box.
template <class T>
static inline void do_not_optimize(const T& value) {
    asm volatile("" : : "g"(&value) : "memory");
}

// Même schéma que le JMH du projet Java : fenêtres de durée fixe, temps moyen par appel en ns.
// Chaque itération relit le fichier puis le parse, comme getResourceAsStream + parse en Java.
static double run_window(const std::string& path, double seconds) {
    using clock = std::chrono::steady_clock;
    auto start = clock::now();
    uint64_t n = 0;
    while (std::chrono::duration<double>(clock::now() - start).count() < seconds) {
        std::string bytes = read_file(path);
        std::vector<Transaction> txs = parse(bytes);
        do_not_optimize(txs);
        ++n;
    }
    double elapsed = std::chrono::duration<double>(clock::now() - start).count();
    return elapsed * 1e9 / static_cast<double>(n);
}

int main(int argc, char** argv) {
    if (argc < 3) {
        std::fprintf(stderr, "usage : camt_bench dump|bench <fichier.xml> [...]\n");
        return 2;
    }
    std::string cmd = argv[1];
    std::string path = argv[2];
    try {
        if (cmd == "dump") {
            std::cout << dump(parse(read_file(path)));
        } else if (cmd == "bench") {
            auto num = [&](int i, double def) { return i < argc ? std::atof(argv[i]) : def; };
            int wn = static_cast<int>(num(3, 3));
            double ws = num(4, 5);
            int mn = static_cast<int>(num(5, 3));
            double ms = num(6, 10);
            for (int i = 1; i <= wn; ++i) {
                std::printf("# warmup %d/%d : %.0f ns/op\n", i, wn, run_window(path, ws));
            }
            double sum = 0;
            for (int i = 1; i <= mn; ++i) {
                double t = run_window(path, ms);
                std::printf("# mesure %d/%d : %.0f ns/op\n", i, mn, t);
                sum += t;
            }
            std::printf("c++ parse : %.0f ns/op (moyenne de %d fenêtres de %g s)\n", sum / mn, mn, ms);
        } else {
            std::fprintf(stderr, "commande inconnue : %s\n", cmd.c_str());
            return 2;
        }
    } catch (const std::exception& e) {
        std::fprintf(stderr, "erreur : %s\n", e.what());
        return 1;
    }
    return 0;
}

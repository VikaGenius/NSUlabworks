package socks.proxy;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.xbill.DNS.ARecord;
import org.xbill.DNS.DClass;
import org.xbill.DNS.Message;
import org.xbill.DNS.Opcode;
import org.xbill.DNS.Section;
import org.xbill.DNS.Type;
import org.xbill.DNS.Record;

public class DnsResolving {
    DatagramChannel dnsChannel;
    private ConcurrentHashMap<Integer, Client> requests = new ConcurrentHashMap<>();

    DnsResolving(DatagramChannel dnsResolver) {
        dnsChannel = dnsResolver;
    }

    public void sendDnsQuery(Client client) throws IOException {
        System.out.println("SENT DNS QUERYYYYYYY");
        // Формирование DNS-запроса для доменного имени
        Message query = new Message();
        query.addRecord(Record.newRecord(org.xbill.DNS.Name.fromString(new String(client.host, StandardCharsets.ISO_8859_1) + "."), Type.A, DClass.IN), Section.QUESTION);
        query.getHeader().setOpcode(Opcode.QUERY);
        requests.put(query.getHeader().getID(), client);

        // Отправка запроса на рекурсивный DNS-резолвер
        dnsChannel.send(ByteBuffer.wrap(query.toWire()), new InetSocketAddress("84.237.50.45", 53));
    }

    public void handleDnsRead(SelectionKey key) throws IOException {
        System.out.println("HANDLE READ DNSSSSSSSSS");
        ByteBuffer buffer = ByteBuffer.allocate(1000);
        dnsChannel.receive(buffer);

        // Обработка DNS-ответа и извлечение IP-адреса
        byte[] responseData = buffer.array();
        Message response = new Message(responseData);
        List<Record> records = response.getSection(Section.ANSWER);
        for (Record record : records) {
            if (record instanceof ARecord) {
                Client client = requests.get(response.getHeader().getID());
                client.ip = ((ARecord) record).getAddress();
                client.openOutsideSock(key.selector());

                break;
            }
        }
    }

}

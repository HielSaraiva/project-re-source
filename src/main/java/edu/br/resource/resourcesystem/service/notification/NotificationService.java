package edu.br.resource.resourcesystem.service.notification;

import edu.br.resource.resourcesystem.model.enums.MatchEventType;
import edu.br.resource.resourcesystem.security.CurrentActor;
import java.time.Instant;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final JdbcTemplate db;
    private final CurrentActor actors;

    public record Owner(boolean institution, int id) {
        public String column() { return institution ? "institution_id" : "user_id"; }
    }
    public record Notice(UUID id, String title, String message, String targetPath, Instant createdAt, boolean read) {}
    public record Inbox(List<Notice> items, long unreadCount, boolean hasMore, int page) {}

    public Owner owner(Authentication auth) {
        boolean institution = auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ONG"));
        return new Owner(institution, institution ? actors.institution(auth).getId() : actors.donor(auth).getId());
    }

    /** Called in the workflow transaction and again by the Rabbit consumer; event/owner uniqueness makes it idempotent. */
    @Transactional
    public void record(UUID eventId) {
        // A completed materialization is a snapshot. Replays must not reinterpret an old receipt
        // against a necessity that was fulfilled by a later donation.
        if (Boolean.TRUE.equals(db.queryForObject(
                "SELECT EXISTS(SELECT 1 FROM user_notifications WHERE event_id=?)", Boolean.class, eventId))) return;
        db.query("SELECT protocol,event_type,donor_id,institution_id,created_at FROM match_event_outbox WHERE id=?", rs -> {
            String protocol=rs.getString("protocol");
            MatchEventType type=MatchEventType.fromValue(rs.getString("event_type"));
            String title=type.getLabel();
            String text=switch(type) {
                case PROPOSAL_SUBMITTED -> "Uma nova proposta de doação está aguardando sua análise.";
                case PROPOSAL_ACCEPTED -> "A ONG aceitou sua proposta. Escolha a modalidade de entrega em até 7 dias.";
                case PROPOSAL_REJECTED -> "A ONG recusou sua proposta. Consulte os detalhes para conhecer o motivo.";
                case DELIVERY_METHOD_SELECTED -> "O doador escolheu a modalidade de entrega. Consulte os próximos passos.";
                case DELIVERY_REPORTED -> "O doador informou a entrega presencial. Confira os itens e confirme o recebimento.";
                case SHIPMENT_REPORTED -> "O doador informou a postagem pelos Correios. Acompanhe o rastreamento.";
                case RECEIPT_CONFIRMED -> "A ONG confirmou o recebimento dos itens. Sua doação foi concluída. Obrigado por contribuir!";
                case DONATION_CANCELLED -> "A doação foi cancelada pelo doador ou pelo encerramento do prazo. Consulte o motivo nos detalhes.";
                case STATUS_CHANGED -> "Há uma atualização no andamento da doação. Consulte os detalhes.";
            };
            boolean donor=switch(type) {case PROPOSAL_ACCEPTED,PROPOSAL_REJECTED,RECEIPT_CONFIRMED,DONATION_CANCELLED,STATUS_CHANGED -> true;default -> false;};
            boolean ong=switch(type) {case PROPOSAL_SUBMITTED,DELIVERY_METHOD_SELECTED,DELIVERY_REPORTED,SHIPMENT_REPORTED,DONATION_CANCELLED,STATUS_CHANGED -> true;default -> false;};
            if (type == MatchEventType.RECEIPT_CONFIRMED && Boolean.TRUE.equals(db.queryForObject("""
                    SELECT coalesce(sum(done.allocated_quantity),0) >= n.quantity_requested
                    FROM match_event_outbox e JOIN matches m ON m.id=e.match_id
                    JOIN necessities n ON n.id=m.necessity_id
                    LEFT JOIN matches done ON done.necessity_id=n.id AND done.status='completed'
                    WHERE e.id=? GROUP BY n.id,n.quantity_requested
                    """, Boolean.class, eventId))) {
                insert(eventId, rs.getInt("institution_id"), true, "Necessidade atendida",
                        "As doações recebidas atenderam a quantidade solicitada. Obrigado por fazer parte dessa rede de impacto!",
                        protocol, rs.getTimestamp("created_at").toInstant());
            }
            if(donor) insert(eventId,rs.getInt("donor_id"),false,title,text,protocol,rs.getTimestamp("created_at").toInstant());
            if(ong) insert(eventId,rs.getInt("institution_id"),true,
                    type==MatchEventType.PROPOSAL_SUBMITTED ? "Nova proposta de doação" : title,text,protocol,rs.getTimestamp("created_at").toInstant());
        },eventId);
    }

    private void insert(UUID event,int owner,boolean ong,String title,String text,String protocol,Instant created) {
        String path=ong ? "/ong/donations/"+protocol : "/donor/donation/status?protocol="+protocol;
        db.update("INSERT INTO user_notifications(id,event_id,"+(ong?"institution_id":"user_id")+",title,message,target_path,created_at) VALUES(?,?,?,?,?,?,?) ON CONFLICT DO NOTHING",
                UUID.randomUUID(),event,owner,title,text+" Protocolo: "+protocol+".",path,java.sql.Timestamp.from(created));
    }

    @Transactional(readOnly=true)
    public Inbox inbox(Owner owner,int page) {
        int safePage=Math.max(0,Math.min(page,10000));
        var items=db.query("SELECT id,title,message,target_path,created_at,read_at FROM user_notifications WHERE "+owner.column()+"=? ORDER BY created_at DESC,id DESC LIMIT 21 OFFSET ?",
                (rs,n)->new Notice(rs.getObject("id",UUID.class),rs.getString("title"),rs.getString("message"),rs.getString("target_path"),rs.getTimestamp("created_at").toInstant(),rs.getTimestamp("read_at")!=null),owner.id(),safePage*20);
        long unread=db.queryForObject("SELECT count(*) FROM user_notifications WHERE "+owner.column()+"=? AND read_at IS NULL",Long.class,owner.id());
        return new Inbox(items.stream().limit(20).toList(),unread,items.size()>20,safePage);
    }

    @Transactional
    public void read(Owner owner,UUID id) {
        int changed=db.update("UPDATE user_notifications SET read_at=coalesce(read_at,now()) WHERE id=? AND "+owner.column()+"=?",id,owner.id());
        if(changed==0) throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Notificação não encontrada.");
    }
    @Transactional
    public void readAll(Owner owner) {
        db.update("UPDATE user_notifications SET read_at=now() WHERE "+owner.column()+"=? AND read_at IS NULL",owner.id());
    }
}

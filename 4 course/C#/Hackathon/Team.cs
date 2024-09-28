using Hackathon.Hackathon;

namespace Hackathon
{
    public class Team
    {
        public Participant Junior { get; set; }
        public Participant TeamLead { get; set; }
        public int IndexPriorityOfJunior { get; set; }
        public int IndexPriorityOfTeamLead { get; set; }

        public Team(Participant junior, Participant teamLead)
        {
            Junior = junior;    
            TeamLead = teamLead;
        }


    }
}
package me.Codex.vvKart.Kart;

public class KartInput {
    // WASD waarden uit STEER_VEHICLE packet (-1..1)
    public float forward = 0f;
    public float sideways = 0f;
    // Optioneel jump (space) voor toekomstige features
    public boolean jump = false;

    // Interne physics state
    public double speed = 0.0; // signed snelheid langs forward-as
    public int idleTicks = 0; // ticks zonder input
}
